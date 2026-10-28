package com.aura.lamplight.tinkerer.charm;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

import java.util.List;

@SuppressWarnings ("all")
public record DodgeCharmEffect(double chance, double cap, List<TagKey<DamageType>> cannotDodge)
		implements CharmEffect {

	private static final List<TagKey<DamageType>> DEFAULT_CANNOT_DODGE = List.of(
		TagKey.create(Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("is_fall")),
		TagKey.create(Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("is_drowning")),
		TagKey.create(Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("is_freezing")),
		TagKey.create(Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("bypasses_armor"))
	);

	public static final Codec<List<TagKey<DamageType>>> TAG_LIST_CODEC =
			Identifier.CODEC.listOf().xmap(
					ids -> ids.stream().map(id -> TagKey.create(Registries.DAMAGE_TYPE, id)).toList(),
					tags -> tags.stream().map(TagKey::location).toList());

	public static final MapCodec<DodgeCharmEffect> MAP_CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
			Codec.doubleRange(0.0, 1.0).fieldOf("chance").forGetter(DodgeCharmEffect::chance),
			Codec.doubleRange(0.0, 1.0).optionalFieldOf("cap", 0.6).forGetter(DodgeCharmEffect::cap),
			TAG_LIST_CODEC.optionalFieldOf("cannot_dodge", DEFAULT_CANNOT_DODGE).forGetter(DodgeCharmEffect::cannotDodge)
		).apply(instance, DodgeCharmEffect::new)
	);

	@Override
	public MapCodec<? extends CharmEffect> codec() {
		return MAP_CODEC;
	}
}
