package com.aura.lamplight.menagerie.crow.ai;

import com.aura.lamplight.menagerie.crow.CrowEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.Goal;

// Mirrors vanilla LandOnOwnersShoulderGoal (the parrot logic): when a follow-mode crow touches its
// owner, it is stored on the shoulder as entity NBT (via ShoulderRidingEntity) and the entity is
// discarded. Vanilla respawns it on dismount (fall, water, elytra, sleep, powder snow) and a
// client layer renders the perched crow. Passenger riding is not used because vanilla startRiding
// rejects non-serializable vehicles server-side, and the PLAYER type is not serializable.
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
        // Follow mode only (command 1); sitting crows never mount, like vanilla.
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
