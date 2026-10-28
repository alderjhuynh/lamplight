package com.aura.lamplight.tinkerer.charm;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings ("all")
public final class CharmSlotAttributes {
	private CharmSlotAttributes() {}

	public static void ensureBases(final Player player) {
		for (CharmSlotKinds.Kind kind : CharmSlotKinds.all()) {
			AttributeInstance max = player.getAttribute(kind.max());
			if (max != null) {
				max.setBaseValue(CharmSlotTypes.definedCount(kind.slug()));
			}
			AttributeInstance current = player.getAttribute(kind.current());
			if (current != null) {
				current.setBaseValue(unlockedCount(player, kind.slug()));
			}
		}
	}

	public static Map<String, Integer> capacities(final Player player) {
		Map<String, Integer> capacities = new HashMap<>();
		for (CharmSlotKinds.Kind kind : CharmSlotKinds.all()) {
			AttributeInstance instance = player.getAttribute(kind.max());
			if (instance != null) {
				capacities.put(kind.slug(), (int) Math.round(instance.getValue()));
			}
		}
		return capacities;
	}

	public static Map<String, Integer> acquired(final Player player) {
		Map<String, Integer> acquired = new HashMap<>();
		for (CharmSlotKinds.Kind kind : CharmSlotKinds.all()) {
			AttributeInstance instance = player.getAttribute(kind.current());
			if (instance != null) {
				acquired.put(kind.slug(), Math.max(0, (int) Math.round(instance.getValue())));
			}
		}
		return acquired;
	}

	public static int unlockedCount(final Player player, final String slug) {
		int unlocked = 0;
		for (CharmSlotTypes.CharmSlotType slot : CharmSlotTypes.all()) {
			if (slot.def().slotType().equals(slug) && CharmAttachments.isUnlocked(player, slot)) {
				unlocked++;
			}
		}
		return unlocked;
	}
}
