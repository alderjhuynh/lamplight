package com.aura.lamplight.client.mixin.armory;

import com.aura.lamplight.armory.DualWieldedPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerDualWieldMixin {
	@Inject(method = "swing", at = @At("HEAD"), cancellable = true)
	private void armory$swingDualWieldHand(InteractionHand hand, CallbackInfo ci) {
		if (hand != InteractionHand.MAIN_HAND || !((Object) this instanceof DualWieldedPlayer dual)) {
			return;
		}
		if (!dual.armory$consumeOffhandSwing()) {
			return;
		}
		ci.cancel();
		((LocalPlayer) (Object) this).swing(InteractionHand.OFF_HAND);
	}
}
