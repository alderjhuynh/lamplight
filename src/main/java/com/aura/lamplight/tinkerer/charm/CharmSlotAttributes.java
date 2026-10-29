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
			int defined = CharmSlotTypes.definedCount(kind.slug());
			AttributeInstance max = player.getAttribute(kind.max());
			if (max != null && max.getBaseValue() < defined) {
				max.setBaseValue(defined);
			}
			AttributeInstance current = player.getAttribute(kind.current());
			int bootstrap = bootstrapCount(player, kind.slug());
			if (current != null && current.getBaseValue() < bootstrap) {
				current.setBaseValue(bootstrap);
			}
		}
	}

	private static int bootstrapCount(final Player player, final String slug) {
		int highest = 0;
		Map<String, Boolean> explicit = player.getAttached(CharmAttachments.UNLOCKED_SLOT_TYPES);
		for (CharmSlotTypes.CharmSlotType slot : CharmSlotTypes.all()) {
			if (!slot.def().slotType().equals(slug)) {
				continue;
			}
			if (!slot.def().unlockedByDefault()) {
				if (explicit == null || !Boolean.TRUE.equals(explicit.get(slot.id().toString()))) {
					continue;
				}
			}
			highest = Math.max(highest, CharmSlotTypes.rankOf(slot));
		}
		return highest;
	}

	public static void ensureUnlock(final Player player, final CharmSlotTypes.CharmSlotType slot) {
		int rank = CharmSlotTypes.rankOf(slot);
		CharmSlotKinds.Kind kind = CharmSlotKinds.bySlug(slot.def().slotType());
		if (rank <= 0 || kind == null) {
			return;
		}
		AttributeInstance current = player.getAttribute(kind.current());
		if (current != null && current.getBaseValue() < rank) {
			current.setBaseValue(rank);
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

	public static int acquiredValue(final Player player, final String slug) {
		CharmSlotKinds.Kind kind = CharmSlotKinds.bySlug(slug);
		if (kind == null) {
			return 0;
		}
		AttributeInstance instance = player.getAttribute(kind.current());
		if (instance == null) {
			return 0;
		}
		return Math.max(0, (int) Math.round(instance.getValue()));
	}
}
