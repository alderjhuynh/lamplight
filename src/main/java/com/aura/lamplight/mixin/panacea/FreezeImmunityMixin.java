package com.aura.lamplight.mixin.panacea;

import com.aura.lamplight.panacea.registry.ModEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class FreezeImmunityMixin {

    @Inject(method = "canFreeze", at = @At("HEAD"), cancellable = true)
    private void panacea$noFreezeAccumulation(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof LivingEntity living && living.hasEffect(ModEffects.FROST_RESISTANCE)) {
            cir.setReturnValue(false);
        }
    }
}
