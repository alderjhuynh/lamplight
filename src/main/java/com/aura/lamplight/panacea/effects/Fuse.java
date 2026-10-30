package com.aura.lamplight.panacea.effects;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;

public class Fuse extends MobEffect {
	public static final float BASE_RADIUS = 2.0F;
	public static final float RADIUS_PER_AMPLIFIER = 2.0F;

	public Fuse(MobEffectCategory category, int color) {
		super(category, color);
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int tickCount, int amplification) {
		// tickCount is the effect's remaining duration, so this fires on the final tick.
		return tickCount == 1;
	}

	@Override
	public boolean applyEffectTick(ServerLevel level, LivingEntity mob, int amplification) {
		float radius = BASE_RADIUS + RADIUS_PER_AMPLIFIER * amplification;
		level.explode(mob, mob.getX(), mob.getY(), mob.getZ(), radius, Level.ExplosionInteraction.NONE);
		// Vanilla excludes the source entity from its own explosion, so the holder
		// detonates too, taking the damage a target standing at the epicentre would
		// take (the epicentre end of ExplosionDamageCalculator#getEntityDamageAmount).
		mob.invulnerableTime = 0;
		mob.hurtServer(level, Explosion.getDefaultDamageSource(level, mob), 14.0F * radius + 1.0F);
		return true;
	}
}
