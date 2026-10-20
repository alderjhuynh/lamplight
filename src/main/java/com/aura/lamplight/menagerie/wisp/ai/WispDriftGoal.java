package com.aura.lamplight.menagerie.wisp.ai;

import com.aura.lamplight.menagerie.wisp.WispEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class WispDriftGoal extends Goal {
    private final WispEntity wisp;
    private double x;
    private double y;
    private double z;
    private int startTick;

    public WispDriftGoal(WispEntity wisp) {
        this.wisp = wisp;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.wisp.isOrderedToSit() || this.wisp.isPassenger()) {
            return false;
        }
        if (this.wisp.isTame() && this.wisp.getOwner() != null) {
            return false;
        }
        if (this.wisp.getRandom().nextInt(35) != 0) {
            return false;
        }
        return this.findTarget();
    }

    private boolean findTarget() {
        for (int attempt = 0; attempt < 10; attempt++) {
            double tx = this.wisp.getX() + this.wisp.getRandom().nextDouble() * 16.0D - 8.0D;
            double ty = this.wisp.getY() + this.wisp.getRandom().nextDouble() * 5.0D - 2.5D;
            double tz = this.wisp.getZ() + this.wisp.getRandom().nextDouble() * 16.0D - 8.0D;

            if (ty < this.wisp.level().getMinY() + 2.0D || ty > this.wisp.level().getMaxY() - 2.0D) {
                continue;
            }
            if (!this.wisp.level().getBlockState(BlockPos.containing(tx, ty, tz)).isAir()) {
                continue;
            }

            Vec3 delta = new Vec3(tx - this.wisp.getX(), ty - this.wisp.getY(), tz - this.wisp.getZ());
            if (!this.wisp.level().noCollision(this.wisp, this.wisp.getBoundingBox().move(delta))) {
                continue;
            }

            this.x = tx;
            this.y = ty;
            this.z = tz;
            return true;
        }
        return false;
    }

    @Override
    public void start() {
        this.startTick = this.wisp.tickCount;
    }

    @Override
    public void tick() {
        this.wisp.getMoveControl().setWantedPosition(this.x, this.y, this.z, 0.05D);
    }

    @Override
    public boolean canContinueToUse() {
        if (this.wisp.isOrderedToSit() || this.wisp.isTame() && this.wisp.getOwner() != null) {
            return false;
        }
        return this.wisp.tickCount - this.startTick < 140
                && this.wisp.distanceToSqr(this.x, this.y, this.z) > 0.8D;
    }
}
