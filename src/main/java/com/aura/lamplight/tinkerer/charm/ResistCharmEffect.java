package com.aura.lamplight.tinkerer.charm;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.damagesource.DamageType;

import java.util.List;

public record ResistCharmEffect(List<Identifier> types, List<TagKey<DamageType>> tags,
                                double fraction, double cap) implements CharmEffect {

	public static final Codec<List<TagKey<DamageType>>> TAG_LIST_CODEC =
			ExtraCodecs.TAG_OR_ELEMENT_ID.listOf().xmap(
					locs -> locs.stream().map(ResistCharmEffect::asTag).toList(),
					tags -> tags.stream().map(tag -> new ExtraCodecs.TagOrElementLocation(tag.location(), true)).toList());

	public static final MapCodec<ResistCharmEffect> MAP_CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
			Identifier.CODEC.listOf().optionalFieldOf("types", List.of()).forGetter(ResistCharmEffect::types),
			ResistCharmEffect.TAG_LIST_CODEC.optionalFieldOf("tags", List.of()).forGetter(ResistCharmEffect::tags),
			Codec.doubleRange(0.0, 1.0).fieldOf("fraction").forGetter(ResistCharmEffect::fraction),
			Codec.doubleRange(0.0, 1.0).optionalFieldOf("cap", 1.0).forGetter(ResistCharmEffect::cap)
		).apply(instance, ResistCharmEffect::new)
	);

	private static TagKey<DamageType> asTag(final ExtraCodecs.TagOrElementLocation location) {
		if (!location.tag()) {
			throw new IllegalArgumentException("Expected a damage type tag with a leading '#', got: " + location);
		}
		return TagKey.create(Registries.DAMAGE_TYPE, location.id());
	}

	public boolean matches(net.minecraft.core.Holder<DamageType> type) {
		for (TagKey<DamageType> tag : this.tags) {
			if (type.is(tag)) {
				return true;
			}
		}
		for (Identifier id : this.types) {
			if (type.is(id)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public MapCodec<? extends CharmEffect> codec() {
		return MAP_CODEC;
	}
}
