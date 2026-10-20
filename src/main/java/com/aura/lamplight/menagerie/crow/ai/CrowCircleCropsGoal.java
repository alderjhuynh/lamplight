package com.aura.lamplight.menagerie.crow.ai;

import com.aura.lamplight.menagerie.crow.CrowBlockPos;
import com.aura.lamplight.menagerie.crow.CrowEntity;
import com.aura.lamplight.menagerie.crow.CrowMaths;
import com.aura.lamplight.menagerie.registry.MenagerieTags;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class CrowCircleCropsGoal extends MoveToBlockGoal {
    private final CrowEntity crow;
    private int idleTime;
    private boolean aboveTarget;
    private float circlingTime;
    private float circleDistance = 2;
    private float yLevel = 2;
    private boolean clockwise;
    private boolean circlePhase;

    public CrowCircleCropsGoal(CrowEntity crow) {
        super(crow, 1.0D, 32, 8);
        this.crow = crow;
    }

    @Override
    public void start() {
        super.start();
        circlePhase = true;
        clockwise = crow.getRandom().nextBoolean();
        yLevel = 1 + crow.getRandom().nextInt(3);
        circleDistance = 1 + crow.getRandom().nextInt(3);
    }

    @Override
    public boolean canUse() {
        return !crow.isBaby() && (crow.getTarget() == null || !crow.getTarget().isAlive())
                && !crow.isTame() && crow.fleePumpkinFlag == 0 && !crow.aiItemFlag && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return blockPos != null && (crow.getTarget() == null || !crow.getTarget().isAlive())
                && !crow.isTame() && !crow.aiItemFlag && crow.fleePumpkinFlag == 0 && super.canContinueToUse();
    }

    @Override
    public void stop() {
        idleTime = 0;
        circlingTime = 0;
        tryTicks = 0;
        blockPos = BlockPos.ZERO;
    }

    @Override
    public double acceptedDistance() {
        return 1.0D;
    }

    @Override
    public void tick() {
        if (blockPos == null) {
            return;
        }
        BlockPos target = this.getMoveToTarget();
        if (circlePhase) {
            this.tryTicks = 0;
            BlockPos circle = getCirclePos(target);
            if (circle != null) {
                crow.setFlying(true);
                crow.getMoveControl().setWantedPosition(circle.getX() + 0.5D, circle.getY() + 0.5D, circle.getZ() + 0.5D, 0.7F);
            }
            circlingTime++;
            if (circlingTime > 200) {
                circlingTime = 0;
                circlePhase = false;
            }
        } else {
            super.tick();
            if (crow.onGround()) {
                crow.setFlying(false);
            }
            if (!withinXZ(target, this.mob.position(), acceptedDistance())) {
                aboveTarget = false;
                ++this.tryTicks;
                this.mob.getNavigation().moveTo(target.getX() + 0.5D, target.getY() - 0.5D, target.getZ() + 0.5D, 1);
            } else {
                aboveTarget = true;
                --this.tryTicks;
            }
            if (isReachedTarget()) {
                crow.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(blockPos.getX() + 0.5D, blockPos.getY(), blockPos.getZ() + 0.5));
                if (idleTime >= 5) {
                    destroyCrop();
                    stop();
                } else {
                    crow.peck();
                    ++idleTime;
                }
            }
        }
    }

    public BlockPos getCirclePos(BlockPos target) {
        float angle = CrowMaths.EIGHT_STARTING_ANGLE * (clockwise ? -circlingTime : circlingTime);
        double extraX = circleDistance * Mth.sin(angle);
        double extraZ = circleDistance * Mth.cos(angle);
        BlockPos pos = CrowBlockPos.fromCoords(target.getX() + 0.5F + extraX, target.getY() + 1 + yLevel, target.getZ() + 0.5F + extraZ);
        if (crow.level().isEmptyBlock(pos)) {
            return pos;
        }
        return null;
    }

    private boolean withinXZ(BlockPos block, Vec3 pos, double dist) {
        return block.distSqr(CrowBlockPos.fromCoords(pos.x(), block.getY(), pos.z())) < dist * dist;
    }

    @Override
    protected boolean isReachedTarget() {
        return aboveTarget;
    }

    private void destroyCrop() {
        if (!canSeeBlock(blockPos)) {
            stop();
            tryTicks = 1200;
            return;
        }
        var level = crow.level();
        boolean grief = level.getServer() == null || level.getServer().getGameRules().get(GameRules.MOB_GRIEFING);
        if (!grief) {
            stop();
            tryTicks = 1200;
            return;
        }
        if (level.getBlockState(blockPos).getBlock() instanceof CropBlock crop) {
            int age = crop.getAge(level.getBlockState(blockPos));
            if (age > 0) {
                level.setBlockAndUpdate(blockPos, crop.getStateForAge(Math.max(0, age - 1)));
            } else {
                level.destroyBlock(blockPos, true);
            }
        } else {
            level.destroyBlock(blockPos, true);
        }
        stop();
        tryTicks = 1200;
    }

    private boolean canSeeBlock(BlockPos pos) {
        Vec3 eye = new Vec3(crow.getX(), crow.getEyeY(), crow.getZ());
        Vec3 blockVec = Vec3.atCenterOf(pos);
        BlockHitResult result = crow.level().clip(new ClipContext(eye, blockVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, crow));
        return result.getBlockPos().equals(pos);
    }

    @Override
    protected boolean isValidTarget(LevelReader level, BlockPos pos) {
        return level.getBlockState(pos).is(MenagerieTags.CROW_FOODBLOCKS);
    }
}
