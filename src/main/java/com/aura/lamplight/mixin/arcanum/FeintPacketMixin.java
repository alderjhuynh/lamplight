package com.aura.lamplight.mixin.arcanum;

import com.aura.lamplight.arcanum.server.RebuildHandler;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class FeintPacketMixin {

    @Inject(method = "handleAnimate", at = @At("HEAD"))
    private void lamplight$rebuildOnSwing(ServerboundSwingPacket packet, CallbackInfo ci) {
        RebuildHandler.onSwing(((ServerGamePacketListenerImpl) (Object) this).player, packet.getHand());
    }

    @Inject(method = "handlePlayerAction", at = @At("HEAD"))
    private void lamplight$rebuildOnPlayerAction(ServerboundPlayerActionPacket packet, CallbackInfo ci) {
        RebuildHandler.onPlayerAction(((ServerGamePacketListenerImpl) (Object) this).player, packet.getAction());
    }
}
