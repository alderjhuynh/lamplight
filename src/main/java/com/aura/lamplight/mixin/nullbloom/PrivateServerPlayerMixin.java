package com.aura.lamplight.mixin.nullbloom;

import com.aura.lamplight.nullbloom.GodAudience;
import com.aura.lamplight.nullbloom.NullbloomDimension;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public class PrivateServerPlayerMixin {
	@Inject(method = "canHarmPlayer", at = @At("HEAD"), cancellable = true)
	private void nullbloom$noPvp(Player target, CallbackInfoReturnable<Boolean> cir) {
		if (NullbloomDimension.areStrangers((ServerPlayer) (Object) this, target) || GodAudience.areStrangers((ServerPlayer) (Object) this, target)) {
			cir.setReturnValue(false);
		}
	}
}
