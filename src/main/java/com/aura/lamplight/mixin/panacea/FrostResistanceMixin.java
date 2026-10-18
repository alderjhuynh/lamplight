package com.aura.lamplight.mixin.panacea;

import com.aura.lamplight.panacea.registry.ModEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class FrostResistanceMixin {

    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    private void panacea$immuneToFreezing(ServerLevel level, DamageSource source, float damage, CallbackInfoReturnable<Boolean> cir) {
        if (source.is(DamageTypeTags.IS_FREEZING) && ((LivingEntity) (Object) this).hasEffect(ModEffects.FROST_RESISTANCE)) {
            cir.setReturnValue(false);
        }
    }
}
