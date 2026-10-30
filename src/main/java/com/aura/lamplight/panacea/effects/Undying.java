package com.aura.lamplight.panacea.effects;

import com.aura.lamplight.panacea.registry.ModAttachments;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class Undying extends MobEffect {

	public Undying(MobEffectCategory category, int color) {
		super(category, color);
	}

	@Override
	public void onEffectStarted(LivingEntity mob, int amplifier) {
		ModAttachments.setCachedUndyingDamage(mob, 0.0F);
	}
}
