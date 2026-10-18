package com.aura.lamplight.armory.client;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

// items that render on the back with the in-hand model instead of the fixed model. 
public final class BackSlotHandItems {
	private static final Set<Identifier> EXTRA_HAND_MODEL_IDS = Set.of(
			Identifier.fromNamespaceAndPath("lamplight", "iron_katana"));

	private BackSlotHandItems() {
	}

	public static boolean usesHandModel(ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}
		if (stack.getItem().builtInRegistryHolder().is(ItemTags.SPEARS)) {
			return true;
		}
		return EXTRA_HAND_MODEL_IDS.contains(BuiltInRegistries.ITEM.getKey(stack.getItem()));
	}
}
