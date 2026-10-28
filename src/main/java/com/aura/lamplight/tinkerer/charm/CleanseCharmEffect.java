package com.aura.lamplight.tinkerer.charm;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;

import java.util.List;

public record CleanseCharmEffect(List<Holder<MobEffect>> effects, double ratePerPotency, double cap)
		implements CharmEffect {

	public static final MapCodec<CleanseCharmEffect> MAP_CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
			MobEffect.CODEC.listOf().fieldOf("effects").forGetter(CleanseCharmEffect::effects),
			Codec.doubleRange(0.0, 1.0).fieldOf("rate_per_potency").forGetter(CleanseCharmEffect::ratePerPotency),
			Codec.doubleRange(0.0, 1.0).optionalFieldOf("cap", 0.4).forGetter(CleanseCharmEffect::cap)
		).apply(instance, CleanseCharmEffect::new)
	);

	public boolean matches(Holder<MobEffect> effect) {
		return this.effects.stream().anyMatch(test -> test.value() == effect.value());
	}

	@Override
	public MapCodec<? extends CharmEffect> codec() {
		return MAP_CODEC;
	}
}
