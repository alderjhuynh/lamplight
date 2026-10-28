package com.aura.lamplight.tinkerer.charm;

import com.aura.lamplight.tinkerer.Tinkerer;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Locale;

@SuppressWarnings ("all")
public final class CharmSlotKinds {
	private CharmSlotKinds() {}

	public record Kind(String slug, Holder<Attribute> current, Holder<Attribute> max) {}

	private static final List<Kind> KINDS = List.of(registerKind("ring"), registerKind("necklace"),
			registerKind("cuff"));

	private static Kind registerKind(final String slug) {
		Attribute current = Registry.register(
				BuiltInRegistries.ATTRIBUTE, Tinkerer.id("charm_slots_" + slug),
				bake("attribute.name.charm_slots_" + slug));
		Attribute max = Registry.register(
				BuiltInRegistries.ATTRIBUTE, Tinkerer.id("max_charm_slots_" + slug),
				bake("attribute.name.max_charm_slots_" + slug));
		return new Kind(slug, BuiltInRegistries.ATTRIBUTE.wrapAsHolder(current),
				BuiltInRegistries.ATTRIBUTE.wrapAsHolder(max));
	}

	private static Attribute bake(final String descriptionId) {
		return new RangedAttribute(descriptionId, 0.0D, 0.0D, 1024.0D)
				.setSyncable(true)
				.setSentiment(Attribute.Sentiment.POSITIVE);
	}

	public static List<Kind> all() {
		return KINDS;
	}

	@Nullable
	public static Kind bySlug(final String slug) {
		for (Kind kind : KINDS) {
			if (kind.slug().equals(slug)) {
				return kind;
			}
		}
		return null;
	}

	public static String slugFor(final Holder<Attribute> attribute) {
		for (Kind kind : KINDS) {
			if (kind.current().equals(attribute) || kind.max().equals(attribute)) {
				return kind.slug();
			}
		}
		return "";
	}

	public static AttributeSupplier.Builder addToPlayerAttributes(final AttributeSupplier.Builder builder) {
		for (Kind kind : KINDS) {
			builder.add(kind.current(), 0.0D).add(kind.max(), 0.0D);
		}
		return builder;
	}

	public static String nameKey(final String slug) {
		return "charm_slot_type.lamplight." + slug.toLowerCase(Locale.ROOT);
	}
}
