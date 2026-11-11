package com.aura.lamplight.armory.client;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("all")
// items that render on the back with the in-hand model instead of the fixed model.
public final class BackSlotHandItems {
	public static final float PIXELS_PER_UNIT = 32.0F;

	public record HandAdjust(float pxRight, float pxUp) {
		public static final HandAdjust NONE = new HandAdjust(0.0F, 0.0F);

		public float unitsRight() {
			return pxRight / PIXELS_PER_UNIT;
		}

		public float unitsUp() {
			return pxUp / PIXELS_PER_UNIT;
		}

		public boolean isZero() {
			return pxRight == 0.0F && pxUp == 0.0F;
		}
	}

	private static final HandAdjust GREATSWORD_ADJUST = new HandAdjust(0.0F, -10.0F);

	private static final String[] SCYTHE_TIERS = {
			"wooden", "stone", "copper", "iron", "golden", "diamond", "netherite"
	};
	private static final String[] GREATSWORD_TIERS = SCYTHE_TIERS;

	private static final Map<Identifier, HandAdjust> EXTRA_HAND_MODELS = buildExtraHandModels();

	private static Map<Identifier, HandAdjust> buildExtraHandModels() {
		Map<Identifier, HandAdjust> map = new HashMap<>();
		map.put(id("iron_katana"), HandAdjust.NONE);
		for (String tier : SCYTHE_TIERS) {
			map.put(id(tier + "_scythe"), HandAdjust.NONE);
		}
		for (String tier : GREATSWORD_TIERS) {
			map.put(id(tier + "_greatsword"), GREATSWORD_ADJUST);
		}
		return Map.copyOf(map);
	}

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath("lamplight", path);
	}

	private BackSlotHandItems() {
	}

	public static boolean usesHandModel(ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}
		if (stack.getItem().builtInRegistryHolder().is(ItemTags.SPEARS)) {
			return true;
		}
		return EXTRA_HAND_MODELS.containsKey(BuiltInRegistries.ITEM.getKey(stack.getItem()));
	}

	public static HandAdjust adjustFor(ItemStack stack) {
		if (stack.isEmpty()) {
			return HandAdjust.NONE;
		}
		return EXTRA_HAND_MODELS.getOrDefault(BuiltInRegistries.ITEM.getKey(stack.getItem()), HandAdjust.NONE);
	}
}