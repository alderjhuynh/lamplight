package com.aura.lamplight.menagerie.raccoon.ai;

import com.aura.lamplight.menagerie.raccoon.RaccoonEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;

public class RaccoonWashGoal extends Goal {
    private static final Direction[] HORIZONTALS = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

    private final RaccoonEntity raccoon;
    @Nullable
    private BlockPos waterPos;
    @Nullable
    private BlockPos targetPos;
    private int washTime = 0;

    public RaccoonWashGoal(RaccoonEntity raccoon) {
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        this.raccoon = raccoon;
    }

    @Override
    public boolean canUse() {
        if (this.raccoon.getMainHandItem().isEmpty() || this.raccoon.isEating()) {
            return false;
        }
        if (this.raccoon.lookForWaterBeforeEatingTimer > 0) {
            this.waterPos = this.generateTarget();
            if (this.waterPos != null) {
                this.targetPos = this.getLandPos(this.waterPos);
                return this.targetPos != null;
            }
        }
        return false;
    }

    @Override
    public void start() {
        this.raccoon.lookForWaterBeforeEatingTimer = 1800;
    }

    @Override
    public void stop() {
        this.targetPos = null;
        this.waterPos = null;
        this.washTime = 0;
        this.raccoon.setWashPos(null);
        this.raccoon.setWashing(false);
        this.raccoon.lookForWaterBeforeEatingTimer = 100;
        this.raccoon.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.targetPos == null || this.waterPos == null) {
            return;
        }
        double dist = this.raccoon.distanceToSqr(Vec3.atCenterOf(this.waterPos));
        if (dist > 2.0D && this.raccoon.isWashing()) {
            this.raccoon.setWashing(false);
        }
        if (dist <= 1.0F) {
            double d0 = this.waterPos.getX() + 0.5D - this.raccoon.getX();
            double d2 = this.waterPos.getZ() + 0.5D - this.raccoon.getZ();
            float yaw = (float) (Mth.atan2(d2, d0) * (180.0D / Math.PI)) - 90.0F;
            this.raccoon.setYRot(yaw);
            this.raccoon.yHeadRot = yaw;
            this.raccoon.yBodyRot = yaw;
            this.raccoon.getNavigation().stop();
            this.raccoon.setWashing(true);
            this.raccoon.setWashPos(this.waterPos);
            this.raccoon.lookForWaterBeforeEatingTimer = 0;
            if (this.washTime % 10 == 0) {
                this.raccoon.gameEvent(GameEvent.BLOCK_ACTIVATE);
                this.raccoon.playSound(SoundEvents.GENERIC_SWIM, 0.7F, 0.5F + this.raccoon.getRandom().nextFloat());
            }
            this.washTime++;
            if (this.washTime > 100) {
                this.stop();
                this.raccoon.beginEatSession();
            }
        } else {
            this.raccoon.getNavigation().moveTo(this.waterPos.getX(), this.waterPos.getY(), this.waterPos.getZ(), 1.2D);
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (this.raccoon.getMainHandItem().isEmpty()) {
            return false;
        }
        return this.targetPos != null && !this.raccoon.isInWater() && this.raccoon.isRaccoonFood(this.raccoon.getMainHandItem());
    }

    @Nullable
    public BlockPos generateTarget() {
        BlockPos blockPos = null;
        RandomSource random = this.raccoon.getRandom();
        int range = 32;
        for (int i = 0; i < 15; i++) {
            BlockPos candidate = this.raccoon.blockPosition().offset(random.nextInt(range) - range / 2, 3, random.nextInt(range) - range / 2);
            while (this.raccoon.level().isEmptyBlock(candidate) && candidate.getY() > this.raccoon.level().getMinY()) {
                candidate = candidate.below();
            }
            if (this.isConnectedToLand(candidate)) {
                blockPos = candidate;
            }
        }
        return blockPos;
    }

    public boolean isConnectedToLand(BlockPos pos) {
        if (this.raccoon.level().getFluidState(pos).is(FluidTags.WATER)) {
            for (Direction dir : HORIZONTALS) {
                BlockPos offsetPos = pos.relative(dir);
                if (this.raccoon.level().getFluidState(offsetPos).isEmpty() && this.raccoon.level().getFluidState(offsetPos.above()).isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Nullable
    public BlockPos getLandPos(BlockPos pos) {
        if (this.raccoon.level().getFluidState(pos).is(FluidTags.WATER)) {
            for (Direction dir : HORIZONTALS) {
                BlockPos offsetPos = pos.relative(dir);
                if (this.raccoon.level().getFluidState(offsetPos).isEmpty() && this.raccoon.level().getFluidState(offsetPos.above()).isEmpty()) {
                    return offsetPos;
                }
            }
        }
        return null;
    }
}
