package com.aura.lamplight.tinkerer.charm;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record CritCharmEffect(double chance, double bonus, double capChance, double capBonus)
		implements CharmEffect {

	public static final MapCodec<CritCharmEffect> MAP_CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
			Codec.doubleRange(0.0, 1.0).fieldOf("chance").forGetter(CritCharmEffect::chance),
			Codec.doubleRange(0.0, 16.0).fieldOf("bonus").forGetter(CritCharmEffect::bonus),
			Codec.doubleRange(0.0, 1.0).optionalFieldOf("cap_chance", 0.75).forGetter(CritCharmEffect::capChance),
			Codec.doubleRange(0.0, 16.0).optionalFieldOf("cap_bonus", 2.0).forGetter(CritCharmEffect::capBonus)
		).apply(instance, CritCharmEffect::new)
	);

	@Override
	public MapCodec<? extends CharmEffect> codec() {
		return MAP_CODEC;
	}
}
