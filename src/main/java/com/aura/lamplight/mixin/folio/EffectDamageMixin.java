package com.aura.lamplight.mixin.folio;

import com.aura.lamplight.folio.effect.DamageEffectHooks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class EffectDamageMixin {

    @ModifyVariable(method = "actuallyHurt", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float folio$livingEffectDamage(
        final float damage,
        final ServerLevel level,
        final DamageSource source,
        final float originalDamage
    ) {
        return DamageEffectHooks.modifyIncomingDamage(level, (LivingEntity) (Object) this, source, damage);
    }
}
