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
        if (!(crow.getOwner() instanceof ServerPlayer owner)) {
            return false;
        }
        if (crow.isSitting() || crow.getCommand() != 1) {
            return false;
        }
        if (owner.isSpectator() || owner.getAbilities().flying || owner.isInWater() || owner.isInPowderSnow) {
            return false;
        }
        return crow.canSitOnShoulder();
    }

    @Override
    public void tick() {
        if (crow.isLeashed() || crow.isRemoved()) {
            return;
        }
        if (crow.getOwner() instanceof ServerPlayer owner
                && !owner.isSpectator()
                && crow.getBoundingBox().intersects(owner.getBoundingBox())) {
            crow.setEntityOnShoulder(owner);
        }
    }
}
