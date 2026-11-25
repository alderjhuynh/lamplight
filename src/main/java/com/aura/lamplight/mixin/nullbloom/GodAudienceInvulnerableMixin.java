package com.aura.lamplight.mixin.nullbloom;

import com.aura.lamplight.nullbloom.GodAudience;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public class GodAudienceInvulnerableMixin {
	@Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
	private void nullbloom$audienceIsUntouchable(ServerLevel level, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
		if (GodAudience.isIn((ServerPlayer) (Object) this) && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
			cir.setReturnValue(true);
		}
	}
}
