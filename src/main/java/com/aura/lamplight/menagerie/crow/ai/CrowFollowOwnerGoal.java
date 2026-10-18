package com.aura.lamplight.menagerie.crow.ai;

import com.aura.lamplight.menagerie.crow.CrowBlockPos;
import com.aura.lamplight.menagerie.crow.CrowEntity;
import com.aura.lamplight.menagerie.crow.CrowMaths;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

// Ported from Alex's Mobs CrowAIFollowOwner (LGPL-3.0, see NOTICE_CROW.md).
// Brings the crow to its owner; perching itself is CrowShoulderGoal (vanilla parrot-style
// shoulder NBT, not passenger riding).
public class CrowFollowOwnerGoal extends Goal {
    private final CrowEntity crow;
    private final LevelReader world;
    private final PathNavigation navigator;
    private final float minDist;
    private final boolean teleportToLeaves;
    private float circleDistance = 1;
    private float yLevel = 2;
    private boolean clockwise;
    private LivingEntity owner;
    private int recalcCooldown;
    private float oldWaterCost;

    public CrowFollowOwnerGoal(CrowEntity crow, double speed, float minDist, float maxDist, boolean teleportToLeaves) {
        this.crow = crow;
        this.world = crow.level();
        this.navigator = crow.getNavigation();
        this.minDist = minDist;
        this.teleportToLeaves = teleportToLeaves;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity owner = this.crow.getOwner();
        if (owner == null || owner.isSpectator()) {
            return false;
        }
        if (this.crow.isSitting() || crow.isPassenger() || crow.getCommand() != 1) {
            return false;
        }
        if (this.crow.distanceToSqr(owner) < (minDist * minDist)) {
            return false;
        }
        this.owner = owner;
        return crow.getTarget() == null || !crow.getTarget().isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        if (this.crow.isSitting()) {
            return false;
        }
        return crow.getCommand() == 1 && !crow.isPassenger() && (crow.getTarget() == null || !crow.getTarget().isAlive());
    }

    @Override
    public void start() {
        this.recalcCooldown = 0;
        this.oldWaterCost = this.crow.getPathfindingMalus(PathType.WATER);
        this.crow.setPathfindingMalus(PathType.WATER, 0.0F);
        clockwise = crow.getRandom().nextBoolean();
        yLevel = crow.getRandom().nextInt(1);
        circleDistance = 1F + crow.getRandom().nextFloat() * 2F;
    }

    @Override
    public void stop() {
        this.owner = null;
        this.navigator.stop();
        this.crow.setPathfindingMalus(PathType.WATER, this.oldWaterCost);
    }

    @Override
    public void tick() {
        this.crow.getLookControl().setLookAt(this.owner, 10.0F, (float) this.crow.getMaxHeadXRot());
        if (this.crow.isLeashed() || this.crow.isPassenger()) {
            return;
        }
        double dist = this.crow.distanceToSqr(this.owner);
        if (--this.recalcCooldown <= 0) {
            this.recalcCooldown = 10;
            if (dist >= 144.0D && !crow.aiItemFlag) {
                crow.setFlying(true);
                crow.getMoveControl().setWantedPosition(owner.getX(), owner.getY() + owner.getEyeHeight() + 0.2F, owner.getZ(), 1F);
                tryTeleportNearOwner();
            }
        }
        if (crow.aiItemFlag) {
            return;
        }
        // Close enough to perch: fly straight into the owner; CrowShoulderGoal mounts on contact
        // (vanilla parrot-style shoulder NBT, not passenger riding).
        if (crow.distanceTo(owner) < 3.0D) {
            crow.getMoveControl().setWantedPosition(owner.getX(), owner.getY() + owner.getEyeHeight() + 0.2F, owner.getZ(), 0.7F);
            return;
        }
        Vec3 circle = getCirclePos(owner.position());
        if (circle == null) {
            circle = owner.position();
        }
        crow.setFlying(true);
        crow.getMoveControl().setWantedPosition(circle.x(), circle.y() + owner.getEyeHeight() + 0.2F, circle.z(), 0.7F);
    }

    public Vec3 getCirclePos(Vec3 target) {
        float angle = CrowMaths.EIGHT_STARTING_ANGLE * (clockwise ? -crow.tickCount : crow.tickCount);
        double extraX = circleDistance * Mth.sin(angle);
        double extraZ = circleDistance * Mth.cos(angle);
        Vec3 pos = new Vec3(target.x() + extraX, target.y() + yLevel, target.z() + extraZ);
        if (crow.level().isEmptyBlock(CrowBlockPos.fromVec3(pos))) {
            return pos;
        }
        return null;
    }

    private void tryTeleportNearOwner() {
        BlockPos base = this.owner.blockPosition();
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
        if (Math.abs(x - this.owner.getX()) < 2.0D && Math.abs(z - this.owner.getZ()) < 2.0D) {
            return false;
        }
        BlockPos pos = new BlockPos(x, y, z);
        if (!isTeleportFriendly(pos)) {
            return false;
        }
        this.crow.snapTo(x + 0.5D, y, z + 0.5D, this.crow.getYRot(), this.crow.getXRot());
        this.navigator.stop();
        return true;
    }

    private boolean isTeleportFriendly(BlockPos pos) {
        BlockState below = this.world.getBlockState(pos.below());
        if (!this.teleportToLeaves && below.getBlock() instanceof LeavesBlock) {
            return false;
        }
        BlockPos offset = pos.subtract(this.crow.blockPosition());
        return this.world.noCollision(this.crow, this.crow.getBoundingBox().move(offset));
    }
}
