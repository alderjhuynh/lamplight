package com.aura.lamplight.menagerie.crow.ai;

import com.aura.lamplight.menagerie.crow.CrowBlockPos;
import com.aura.lamplight.menagerie.crow.CrowEntity;
import com.aura.lamplight.menagerie.crow.CrowMath;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class CrowFollowOwnerGoal extends Goal {
    private final CrowEntity crow;
    private final boolean teleportToLeaves;
    private float circleDistance = 1;
    private final float yLevel = 0;
    private boolean clockwise;
    private LivingEntity owner;
    private int recalcCooldown;
    private float oldWaterCost;

    public CrowFollowOwnerGoal(CrowEntity crow, boolean teleportToLeaves) {
        this.crow = crow;
        this.teleportToLeaves = teleportToLeaves;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity owner = crow.getOwner();
        if (owner == null || !owner.isAlive() || owner.isSpectator()
                || owner.level() != crow.level()) {
            return false;
        }
        if (!crow.isFollowing() || crow.isPassenger() || crow.isInLove()) {
            return false;
        }
        if (crow.getTarget() != null && crow.getTarget().isAlive()) {
            return false;
        }
        this.owner = owner;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return crow.isFollowing()
                && !crow.isPassenger()
                && !crow.isInLove()
                && owner != null
                && owner.isAlive()
                && !owner.isSpectator()
                && owner.level() == crow.level()
                && crow.getOwner() == owner
                && (crow.getTarget() == null || !crow.getTarget().isAlive());
    }

    @Override
    public void start() {
        recalcCooldown = 0;
        oldWaterCost = crow.getPathfindingMalus(PathType.WATER);
        crow.setPathfindingMalus(PathType.WATER, 0.0F);
        clockwise = crow.getRandom().nextBoolean();
        circleDistance = 1F + crow.getRandom().nextFloat() * 2F;
    }

    @Override
    public void stop() {
        owner = null;
        crow.getNavigation().stop();
        crow.setPathfindingMalus(PathType.WATER, oldWaterCost);
    }

    @Override
    public void tick() {
        if (owner == null || !owner.isAlive()) {
            stop();
            return;
        }
        crow.getLookControl().setLookAt(owner, 10.0F, crow.getMaxHeadXRot());
        if (crow.isLeashed() || crow.isPassenger()) {
            return;
        }

        double distSq = crow.distanceToSqr(owner);
        if (--recalcCooldown <= 0) {
            recalcCooldown = 10;
            if (distSq >= 144.0D) {
                crow.setFlying(true);
                crow.getMoveControl().setWantedPosition(
                        owner.getX(), owner.getY() + owner.getEyeHeight() + 0.2F, owner.getZ(), 1.0F);
                tryTeleportNearOwner();
            }
        }

        if (distSq < 9.0D) {
            if (canShoulderMount(owner)) {
                crow.setFlying(true);
                if (distSq <= 6.25D) {
                    crow.getMoveControl().setWantedPosition(
                            crow.getX(), crow.getY(), crow.getZ(), 0.0F);
                } else {
                    crow.getMoveControl().setWantedPosition(
                            owner.getX(), owner.getY() + owner.getEyeHeight() + 0.2F,
                            owner.getZ(), 0.7F);
                }
                return;
            }
            Vec3 closeCircle = getCirclePos(owner.position());
            if (closeCircle != null) {
                crow.setFlying(true);
                crow.getMoveControl().setWantedPosition(
                        closeCircle.x(), closeCircle.y() + owner.getEyeHeight() + 0.2F,
                        closeCircle.z(), 0.7F);
            } else {
                crow.setFlying(true);
                crow.getMoveControl().setWantedPosition(
                        crow.getX(), crow.getY(), crow.getZ(), 0.0F);
            }
            return;
        }

        Vec3 circle = getCirclePos(owner.position());
        if (circle == null) {
            circle = owner.position();
        }
        crow.setFlying(true);
        crow.getMoveControl().setWantedPosition(
                circle.x(), circle.y() + owner.getEyeHeight() + 0.2F, circle.z(), 0.7F);
    }

    private boolean canShoulderMount(LivingEntity owner) {
        if (!(owner instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        return serverPlayer.isAlive()
                && serverPlayer.level() == crow.level()
                && !serverPlayer.isSpectator()
                && !serverPlayer.getAbilities().flying
                && !serverPlayer.isInWater()
                && !serverPlayer.isInPowderSnow
                && !crow.isLeashed()
                && !crow.isInLove()
                && crow.canSitOnShoulder()
                && crow.hasLineOfSight(serverPlayer);
    }

    public Vec3 getCirclePos(Vec3 target) {
        float angle = CrowMath.EIGHT_STARTING_ANGLE * (clockwise ? -crow.tickCount : crow.tickCount);
        double extraX = circleDistance * Mth.sin(angle);
        double extraZ = circleDistance * Mth.cos(angle);
        Vec3 pos = new Vec3(target.x() + extraX, target.y() + yLevel, target.z() + extraZ);
        if (crow.level().isEmptyBlock(CrowBlockPos.fromVec3(pos))) {
            return pos;
        }
        return null;
    }

    private void tryTeleportNearOwner() {
        BlockPos base = owner.blockPosition();
        for (int i = 0; i < 10; i++) {
            int dx = crow.getRandom().nextInt(7) - 3;
            int dy = crow.getRandom().nextInt(3) - 1;
            int dz = crow.getRandom().nextInt(7) - 3;
            if (tryTeleport(base.getX() + dx, base.getY() + dy, base.getZ() + dz)) {
                return;
            }
        }
    }

    private boolean tryTeleport(int x, int y, int z) {
        if (Math.abs(x - owner.getX()) < 2.0D && Math.abs(z - owner.getZ()) < 2.0D) {
            return false;
        }
        BlockPos pos = new BlockPos(x, y, z);
        if (!isTeleportFriendly(pos)) {
            return false;
        }
        crow.snapTo(x + 0.5D, y, z + 0.5D, crow.getYRot(), crow.getXRot());
        crow.getNavigation().stop();
        return true;
    }

    private boolean isTeleportFriendly(BlockPos pos) {
        BlockState below = crow.level().getBlockState(pos.below());
        if (!teleportToLeaves && below.getBlock() instanceof LeavesBlock) {
            return false;
        }
        BlockPos offset = pos.subtract(crow.blockPosition());
        return crow.level().noCollision(crow, crow.getBoundingBox().move(offset));
    }
}
