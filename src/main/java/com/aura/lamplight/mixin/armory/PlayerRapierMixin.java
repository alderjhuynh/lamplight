package com.aura.lamplight.mixin.armory;

import com.aura.lamplight.armory.RapierCombat;
import com.aura.lamplight.armory.RapierItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerRapierMixin {

	@Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
	private void armory$rapierParry(ServerLevel level, DamageSource source, float damage,
	                                CallbackInfoReturnable<Boolean> cir) {
		if ((Object) this instanceof ServerPlayer player && RapierCombat.tryParry(player, level, source)) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "canCriticalAttack", at = @At("HEAD"), cancellable = true)
	private void armory$rapierRiposteCrit(Entity target, CallbackInfoReturnable<Boolean> cir) {
		if (RapierCombat.hasRiposte((Player) (Object) this)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "getAttackStrengthScale", at = @At("HEAD"), cancellable = true)
	private void armory$rapierRipostePower(float a, CallbackInfoReturnable<Float> cir) {
		if (RapierCombat.hasRiposte((Player) (Object) this)) {
			cir.setReturnValue(1.0F);
		}
	}

	@Inject(method = "isSweepAttack", at = @At("HEAD"), cancellable = true)
	private void armory$rapierThrustNoSweep(boolean fullStrengthAttack, boolean criticalAttack, boolean knockbackAttack,
	                                        CallbackInfoReturnable<Boolean> cir) {
		Player self = (Player) (Object) this;
		if (RapierItem.isRapier(self.getMainHandItem()) && RapierCombat.isThrustReady(self)) {
			cir.setReturnValue(false);
		}
	}
}
