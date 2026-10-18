package com.aura.lamplight.mixin.panacea;

import com.aura.lamplight.panacea.registry.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class ShroudedDetectionMixin {

    @Inject(method = "getVisibilityPercent", at = @At("RETURN"), cancellable = true)
    private void panacea$shroudedIgnoresEquipment(@Nullable Entity targetingEntity, CallbackInfoReturnable<Double> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        MobEffectInstance invisibility = self.getEffect(MobEffects.INVISIBILITY);
        if (invisibility == null || invisibility.getAmplifier() < 1) {
            return;
        }

        double result = cir.getReturnValue();
        float coverPercentage = self.getArmorCoverPercentage();
        if (coverPercentage < 0.1F) {
            coverPercentage = 0.1F;
        }
        cir.setReturnValue(result * 0.1 / coverPercentage);
    }
}
