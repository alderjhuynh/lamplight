package com.aura.lamplight.tinkerer.charm;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.List;

public record CharmTemplate(Identifier item, String slotType, int insetCount, double focus,
                            boolean unlockedByDefault, List<CharmTemplateOverride> overrides) {

	public static final Codec<CharmTemplate> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
			Identifier.CODEC.fieldOf("item").forGetter(CharmTemplate::item),
			Codec.STRING.fieldOf("slot_type").forGetter(CharmTemplate::slotType),
			Codec.intRange(1, 24).optionalFieldOf("inset_count", 1).forGetter(CharmTemplate::insetCount),
			Codec.DOUBLE.optionalFieldOf("focus", 1.0).forGetter(CharmTemplate::focus),
			Codec.BOOL.optionalFieldOf("unlocked_by_default", false).forGetter(CharmTemplate::unlockedByDefault),
			CharmTemplateOverride.CODEC.listOf().optionalFieldOf("overrides", List.of()).forGetter(CharmTemplate::overrides)
		).apply(instance, CharmTemplate::new)
	);

	public record CharmTemplateOverride(Identifier inset, List<CharmEffect> effects) {
		public static final Codec<CharmTemplateOverride> CODEC = RecordCodecBuilder.create(
			i -> i.group(
				Identifier.CODEC.fieldOf("inset").forGetter(CharmTemplateOverride::inset),
				CharmEffect.CODEC.listOf().fieldOf("effects").forGetter(CharmTemplateOverride::effects)
			).apply(i, CharmTemplateOverride::new)
		);
	}

	public List<CharmEffect> effectsFor(Identifier insetItem, List<CharmEffect> fallback) {
		for (CharmTemplateOverride override : overrides) {
			if (override.inset().equals(insetItem)) {
				return override.effects();
			}
		}
		return fallback;
	}
}
