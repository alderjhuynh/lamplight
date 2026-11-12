package com.aura.lamplight.mixin.arcanum;

import com.aura.lamplight.arcanum.server.FlowGlideHandler;
import com.aura.lamplight.arcanum.server.JetServerHandler;
import com.aura.lamplight.arcanum.server.RebuildHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityFallMixin {

    @Inject(method = "causeFallDamage", at = @At("HEAD"), cancellable = true)
    private void arcanum$cancelFallDamageWhileGliding(double fallDistance, float damageMultiplier, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof ServerPlayer sp) {
            if (JetServerHandler.isGliding(sp) || FlowGlideHandler.isGliding(sp)) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "causeFallDamage", at = @At("RETURN"))
    private void arcanum$stickyLandingBounce(double fallDistance, float damageMultiplier, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        RebuildHandler.onFallLanded((LivingEntity) (Object) this, (float) fallDistance);
    }
}
