package com.aura.lamplight.menagerie.wisp.ai;

import com.aura.lamplight.menagerie.wisp.WispEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class WispShoalGoal extends Goal {
    private final WispEntity wisp;
    private WispEntity neighbour;
    private int updateTimer;

    public WispShoalGoal(WispEntity wisp) {
        this.wisp = wisp;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.wisp.isTame() || this.wisp.isOrderedToSit() || this.wisp.getRandom().nextInt(35) != 0) {
            return false;
        }

        List<WispEntity> nearby = this.wisp.level().getEntitiesOfClass(
                WispEntity.class,
                this.wisp.getBoundingBox().inflate(16.0D),
                other -> other != this.wisp && other.isAlive() && !other.isTame());
        if (nearby.isEmpty()) {
            return false;
        }
        nearby.sort(Comparator.comparingDouble(this.wisp::distanceToSqr));
        this.neighbour = nearby.getFirst();
        return this.wisp.distanceToSqr(this.neighbour) > 16.0D;
    }

    @Override
    public boolean canContinueToUse() {
        return !this.wisp.isTame()
                && this.wisp.getStartleTicks() <= 0
                && this.neighbour != null
                && this.neighbour.isAlive()
                && this.wisp.distanceToSqr(this.neighbour) > 9.0D;
    }

    @Override
    public void start() {
        this.updateTimer = 0;
    }

    @Override
    public void tick() {
        if (this.neighbour == null) {
            return;
        }
        if (--this.updateTimer > 0) {
            return;
        }
        this.updateTimer = 10;
        this.wisp.getMoveControl().setWantedPosition(
                this.neighbour.getX(),
                this.neighbour.getY() + 0.4D,
                this.neighbour.getZ(),
                0.075D);
    }

    @Override
    public void stop() {
        this.neighbour = null;
    }
}
