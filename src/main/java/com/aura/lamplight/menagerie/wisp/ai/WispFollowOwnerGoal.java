package com.aura.lamplight.menagerie.wisp.ai;

import com.aura.lamplight.menagerie.wisp.WispEntity;
import com.aura.lamplight.menagerie.wisp.WispPulseControl;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class WispFollowOwnerGoal extends Goal {
    private static final double TELEPORT_DISTANCE_SQR = 16.0D * 16.0D;
    private static final double MAX_AURA_DISTANCE_SQR = 10.0D * 10.0D;

    private final WispEntity wisp;
    private LivingEntity owner;
    private int updateTimer;
    private int teleportCooldown;

    public WispFollowOwnerGoal(WispEntity wisp) {
        this.wisp = wisp;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!this.wisp.isTame() || this.wisp.isOrderedToSit() || this.wisp.isPassenger()) {
            return false;
        }
        LivingEntity owner = this.wisp.getOwner();
        if (owner == null || !owner.isAlive() || owner.isSpectator() || owner.level() != this.wisp.level()) {
            return false;
        }
        this.owner = owner;
        return this.wisp.distanceToSqr(owner) > MAX_AURA_DISTANCE_SQR || this.wisp.getMoveControl().hasWanted();
    }

    @Override
    public boolean canContinueToUse() {
        return this.owner != null
                && this.wisp.isTame()
                && !this.wisp.isOrderedToSit()
                && this.owner.isAlive()
                && this.owner.level() == this.wisp.level();
    }

    @Override
    public void start() {
        this.updateTimer = 0;
        this.teleportCooldown = 0;
    }

    @Override
    public void tick() {
        if (this.owner == null) {
            return;
        }

        if (this.teleportCooldown > 0) {
            this.teleportCooldown--;
        }

        if (this.wisp.distanceToSqr(this.owner) > TELEPORT_DISTANCE_SQR && this.teleportCooldown == 0) {
            if (this.tryTeleportNearOwner()) {
                this.teleportCooldown = 40;
            }
            return;
        }

        if (--this.updateTimer > 0) {
            return;
        }
        this.updateTimer = 4;

        Vec3 look = this.owner.getLookAngle();
        Vec3 side = new Vec3(-look.z, 0.0D, look.x);
        if (side.lengthSqr() < 1.0E-4D) {
            side = new Vec3(1.0D, 0.0D, 0.0D);
        }
        side = side.normalize().scale(0.9D);

        Vec3 target = this.owner.getEyePosition().add(side).add(0.0D, 0.35D, 0.0D);
        this.wisp.getMoveControl().setWantedPosition(target.x, target.y, target.z, 0.18D);
    }

    private boolean tryTeleportNearOwner() {
        for (int attempt = 0; attempt < 12; attempt++) {
            double x = this.owner.getX() + this.wisp.getRandom().nextDouble() * 6.0D - 3.0D;
            double y = this.owner.getEyeY() + this.wisp.getRandom().nextDouble() * 2.0D - 0.5D;
            double z = this.owner.getZ() + this.wisp.getRandom().nextDouble() * 6.0D - 3.0D;

            Vec3 delta = new Vec3(x - this.wisp.getX(), y - this.wisp.getY(), z - this.wisp.getZ());
            if (this.wisp.level().noCollision(this.wisp, this.wisp.getBoundingBox().move(delta))) {
                this.wisp.snapTo(x, y, z, this.wisp.getYRot(), this.wisp.getXRot());
                ((WispPulseControl) this.wisp.getMoveControl()).setWait();
                return true;
            }
        }
        return false;
    }

    @Override
    public void stop() {
        this.owner = null;
    }
}
