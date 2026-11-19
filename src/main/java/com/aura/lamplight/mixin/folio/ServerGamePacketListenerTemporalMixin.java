package com.aura.lamplight.mixin.folio;

import com.aura.lamplight.folio.temporal.TemporalFields;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerTemporalMixin {
    @Shadow
    public ServerPlayer player;

    @Inject(method = "tickPlayer", at = @At("HEAD"), cancellable = true)
    private void lamplight$skipSlowPlayerTick(CallbackInfoReturnable<Boolean> cir) {
        if (player != null && !player.level().isClientSide() && !TemporalFields.shouldTick(player)) {
            cir.setReturnValue(false);
        }
    }
}
