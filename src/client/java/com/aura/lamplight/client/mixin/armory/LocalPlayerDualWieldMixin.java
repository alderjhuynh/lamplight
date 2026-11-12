package com.aura.lamplight.client.mixin.armory;

import com.aura.lamplight.armory.DualWielded;
import com.aura.lamplight.puppeteer.client.LocalActionDetector;
import net.minecraft.client.Minecraft;
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
		if (hand != InteractionHand.MAIN_HAND) {
			return;
		}
		LocalPlayer self = (LocalPlayer) (Object) this;
		if (!LocalActionDetector.isOffhandAttackTurn()
				|| !Minecraft.getInstance().options.keyAttack.isDown()
				|| !DualWielded.isActive(self)
				|| !DualWielded.hasGhost(self)) {
			return;
		}
		ci.cancel();
		self.swing(InteractionHand.OFF_HAND);
	}
}
