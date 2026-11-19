package com.aura.lamplight.mixin.folio;

import com.aura.lamplight.folio.temporal.TemporalFields;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelTemporalMixin {
    @Inject(method = "tickNonPassenger", at = @At("HEAD"), cancellable = true)
    private void lamplight$skipSlowEntityTick(Entity entity, CallbackInfo ci) {
        if (!TemporalFields.shouldTick(entity)) {
            ci.cancel();
        }
    }

    @Inject(
        method = "tickPassenger",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setOldPosAndRot()V"),
        cancellable = true
    )
    private void lamplight$skipSlowPassengerTick(Entity vehicle, Entity passenger, CallbackInfo ci) {
        if (!TemporalFields.shouldTick(passenger)) {
            ci.cancel();
        }
    }
}
