package com.aura.lamplight.tinkerer.charm;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record CharmMaterial(Identifier item, double settingMultiplier, int settingCost, int insetCost,
                            List<CharmEffect> insets) {

	public static final int DEFAULT_SETTING_COST = 8;
	public static final int DEFAULT_INSET_COST = 4;

	public static final Codec<CharmMaterial> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
			Identifier.CODEC.fieldOf("item").forGetter(CharmMaterial::item),
			Codec.DOUBLE.optionalFieldOf("setting_multiplier", 1.0).forGetter(CharmMaterial::settingMultiplier),
			Codec.intRange(1, 64).optionalFieldOf("setting_cost", DEFAULT_SETTING_COST).forGetter(CharmMaterial::settingCost),
			Codec.intRange(1, 64).optionalFieldOf("inset_cost", DEFAULT_INSET_COST).forGetter(CharmMaterial::insetCost),
			CharmEffect.CODEC.listOf().fieldOf("insets").forGetter(CharmMaterial::insets)
		).apply(instance, CharmMaterial::new)
	);

	public boolean matches(ItemStack stack) {
		return !stack.isEmpty() && BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(item);
	}
}
