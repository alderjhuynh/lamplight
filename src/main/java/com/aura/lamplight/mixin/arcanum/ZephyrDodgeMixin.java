package com.aura.lamplight.mixin.arcanum;

import com.aura.lamplight.arcanum.server.CapstoneHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class ZephyrDodgeMixin {

    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    private void lamplight$zephyrDodge(ServerLevel level, DamageSource source, float amount,
                                       CallbackInfoReturnable<Boolean> cir) {
        if (CapstoneHandler.tryDodge((LivingEntity) (Object) this, source)) {
            cir.setReturnValue(false);
        }
    }
}
