package com.aura.lamplight.tinkerer.charm;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public record AttributeCharmEffect(Holder<Attribute> attribute, AttributeModifier.Operation operation, double amount)
		implements CharmEffect {

	public static final MapCodec<AttributeCharmEffect> MAP_CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
			Attribute.CODEC.fieldOf("attribute").forGetter(AttributeCharmEffect::attribute),
			AttributeModifier.Operation.CODEC.optionalFieldOf("operation", AttributeModifier.Operation.ADD_VALUE)
					.forGetter(AttributeCharmEffect::operation),
			Codec.DOUBLE.fieldOf("amount").forGetter(AttributeCharmEffect::amount)
		).apply(instance, AttributeCharmEffect::new)
	);

	@Override
	public MapCodec<? extends CharmEffect> codec() {
		return MAP_CODEC;
	}
}
