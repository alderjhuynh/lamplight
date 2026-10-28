package com.aura.lamplight.tinkerer.charm;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;

import java.util.List;

@SuppressWarnings ("all")
public record IgniteCharmEffect(double chance, double seconds, double capChance, double capSeconds)
		implements CharmEffect {

	public static final MapCodec<IgniteCharmEffect> MAP_CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
			Codec.doubleRange(0.0, 1.0).fieldOf("chance").forGetter(IgniteCharmEffect::chance),
			Codec.doubleRange(0.0, 64.0).fieldOf("seconds").forGetter(IgniteCharmEffect::seconds),
			Codec.doubleRange(0.0, 1.0).optionalFieldOf("cap_chance", 0.5).forGetter(IgniteCharmEffect::capChance),
			Codec.doubleRange(0.0, 64.0).optionalFieldOf("cap_seconds", 8.0).forGetter(IgniteCharmEffect::capSeconds)
		).apply(instance, IgniteCharmEffect::new)
	);

	@Override
	public MapCodec<? extends CharmEffect> codec() {
		return MAP_CODEC;
	}
}
