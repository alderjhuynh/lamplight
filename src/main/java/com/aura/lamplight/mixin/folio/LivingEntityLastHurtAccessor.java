package com.aura.lamplight.mixin.folio;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityLastHurtAccessor {
    @Accessor("lastHurt")
    float folio$getLastHurt();

    @Accessor("lastHurt")
    void folio$setLastHurt(float lastHurt);
}
