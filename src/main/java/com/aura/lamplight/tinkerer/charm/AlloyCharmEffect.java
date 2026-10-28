package com.aura.lamplight.tinkerer.charm;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record AlloyCharmEffect(double bonusPerPotency, double cap) implements CharmEffect {

	public static final MapCodec<AlloyCharmEffect> MAP_CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
			Codec.doubleRange(0.0, 16.0).fieldOf("bonus_per_potency").forGetter(AlloyCharmEffect::bonusPerPotency),
			Codec.doubleRange(0.0, 16.0).optionalFieldOf("cap", 1.0).forGetter(AlloyCharmEffect::cap)
		).apply(instance, AlloyCharmEffect::new)
	);

	@Override
	public MapCodec<? extends CharmEffect> codec() {
		return MAP_CODEC;
	}
}
