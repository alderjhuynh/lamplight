package com.aura.lamplight.menagerie.crow.ai;

import com.aura.lamplight.menagerie.crow.CrowEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.Goal;

public class CrowShoulderGoal extends Goal {
    private final CrowEntity crow;

    public CrowShoulderGoal(CrowEntity crow) {
        this.crow = crow;
    }

    @Override
    public boolean canUse() {
        if (crow.isDelivering() || crow.isFetching() || !crow.isFollowing() || crow.isSitting() || crow.isLeashed() || crow.isPassenger()) {
            return false;
        }
        if (crow.isInLove()) {
            return false;
        }
        if (crow.getTarget() != null && crow.getTarget().isAlive()) {
            return false;
        }
        if (!(crow.getOwner() instanceof ServerPlayer owner)) {
            return false;
        }
        if (!owner.isAlive() || owner.level() != crow.level() || owner.isSpectator()
                || owner.getAbilities().flying || owner.isInWater() || owner.isInPowderSnow) {
            return false;
        }
        return crow.canSitOnShoulder();
    }

    @Override
    public void tick() {
        if (crow.isDelivering() || crow.isFetching() || crow.isLeashed() || crow.isRemoved()
                || crow.isSitting() || crow.isPassenger()) {
            return;
        }
        if (crow.getOwner() instanceof ServerPlayer owner
                && owner.isAlive()
                && owner.level() == crow.level()
                && !owner.isSpectator()
                && !owner.getAbilities().flying
                && !owner.isInWater()
                && !owner.isInPowderSnow
                && !crow.isInLove()
                && (crow.getTarget() == null || !crow.getTarget().isAlive())
                && crow.distanceToSqr(owner) <= 6.25D
                && crow.hasLineOfSight(owner)) {
            crow.setEntityOnShoulder(owner);
        }
    }
}
