package com.aura.lamplight.folio.effect;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class Frozen {
    private Frozen() {}

    public static boolean is(final Entity entity) {
        return entity instanceof LivingEntity living && living.hasEffect(FolioEffects.FROZEN);
    }
}
