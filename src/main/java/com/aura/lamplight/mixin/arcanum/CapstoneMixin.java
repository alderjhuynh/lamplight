package com.aura.lamplight.mixin.arcanum;

import com.aura.lamplight.arcanum.server.CapstoneHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class CapstoneMixin {

    @Inject(method = "hurtServer", at = @At("RETURN"))
    private void lamplight$capstonesOnHurt(ServerLevel level, DamageSource source, float amount,
                                           CallbackInfoReturnable<Boolean> cir) {
        CapstoneHandler.onHurt(level, (LivingEntity) (Object) this, source, cir.getReturnValue());
    }

    @Inject(method = "die", at = @At("HEAD"))
    private void lamplight$capstonesOnDeath(DamageSource source, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.level() instanceof ServerLevel level) {
            CapstoneHandler.onDeath(level, self);
        }
    }
}
