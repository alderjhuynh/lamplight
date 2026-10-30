package com.aura.lamplight.mixin.panacea;

import com.aura.lamplight.panacea.registry.ModAttachments;
import com.aura.lamplight.panacea.registry.ModEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(LivingEntity.class)
public abstract class UndyingMixin {

	@Shadow
	protected float lastHurt;

	@Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
	private void panacea$cacheDamageWhileUndying(ServerLevel level, DamageSource source, float damage, CallbackInfoReturnable<Boolean> cir) {
		if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
			return;
		}

		LivingEntity self = (LivingEntity) (Object) this;
		if (self.isInvulnerableTo(level, source)
				|| self.isDeadOrDying()
				|| (source.is(DamageTypeTags.IS_FIRE) && self.hasEffect(MobEffects.FIRE_RESISTANCE))) {
			return;
		}

		MobEffectInstance undying = self.getEffect(ModEffects.UNDYING);
		if (undying != null) {
			float applied = damage;
			if (self.invulnerableTime > 10 && !source.is(DamageTypeTags.BYPASSES_COOLDOWN)) {
				if (damage <= this.lastHurt) {
					cir.setReturnValue(false);
					return;
				}
				applied = damage - this.lastHurt;
			}

			ModAttachments.setCachedUndyingDamage(self, ModAttachments.cachedUndyingDamage(self) + applied);
			this.lastHurt = damage;
			self.invulnerableTime = 20;
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "onEffectsRemoved", at = @At("TAIL"))
	private void panacea$applyCachedDamageOnRemoval(Collection<MobEffectInstance> removed, CallbackInfo ci) {
		LivingEntity self = (LivingEntity) (Object) this;
		boolean undyingRemoved = false;
		for (MobEffectInstance instance : removed) {
			if (instance.is(ModEffects.UNDYING)) {
				undyingRemoved = true;
				break;
			}
		}
		if (!undyingRemoved) {
			return;
		}

		if (!(self.level() instanceof ServerLevel level)) {
			return;
		}

		float cached = ModAttachments.cachedUndyingDamage(self);
		ModAttachments.setCachedUndyingDamage(self, 0.0F);
		if (cached <= 0.0F) {
			return;
		}

		self.invulnerableTime = 0;
		self.hurtServer(level, level.damageSources().generic(), cached);
	}
}
