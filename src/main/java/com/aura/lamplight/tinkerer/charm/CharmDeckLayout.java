package com.aura.lamplight.tinkerer.charm;

import com.aura.lamplight.tinkerer.CharmItem;
import com.aura.lamplight.tinkerer.Tinkerer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings ("all")
public final class CharmDeckLayout {
	private CharmDeckLayout() {}

	public static final int WELL_X = 8;
	public static final int TOP_CAP = 8;
	public static final int WELL_PITCH = 18;
	public static final int PANEL_WIDTH = 32;
	public static final int FOOTER_UNIT = 24;

	public record Well(Identifier id, CharmSlotTypes.@Nullable CharmSlotType def, String slug, boolean granted) {}

	private static volatile Map<String, Integer> clientCapacities = Map.of();

	private static volatile Map<String, Integer> clientAcquired = Map.of();

	public static void setClientCapacities(final Map<String, Integer> capacities) {
		clientCapacities = Map.copyOf(capacities);
	}

	public static void setClientAcquired(final Map<String, Integer> acquired) {
		clientAcquired = Map.copyOf(acquired);
	}

	public static Map<String, Integer> clientCapacities() {
		return clientCapacities;
	}

	private static volatile @Nullable List<Well> clientWells;

	public static void setClientWells(final List<Identifier> ids) {
		List<Well> built = new ArrayList<>();
		for (Identifier id : ids) {
			CharmSlotTypes.CharmSlotType def = CharmSlotTypes.byId(id);
			if (def != null) {
				built.add(new Well(id, def, def.def().slotType(), false));
				continue;
			}
			String path = id.getPath();
			int split = path.lastIndexOf("_grant_");
			if (split > 0 && !path.isEmpty()
					&& path.substring(split + "_grant_".length()).chars().allMatch(Character::isDigit)) {
				String slug = path.substring(0, split);
				built.add(new Well(id, null, slug, true));
			}
		}
		clientWells = List.copyOf(built);
	}

	public static @Nullable List<Well> clientWells() {
		return clientWells;
	}

	public static @Nullable List<Identifier> clientWellIds() {
		List<Well> wells = clientWells;
		if (wells == null) {
			return null;
		}
		return wells.stream().map(Well::id).toList();
	}

	public static Map<String, Integer> clientAcquired() {
		return clientAcquired;
	}

	public static List<Well> wells(final Player player, final Map<String, Integer> capacities,
	                               final Map<String, Integer> acquired) {
		if (player.level().isClientSide() && clientWells != null) {
			return clientWells;
		}
		final List<CharmSlotTypes.CharmSlotType> defined = CharmSlotTypes.all();
		if (defined.isEmpty()) {
			return List.of();
		}
		final Map<String, Integer> definedRank = new HashMap<>();
		final Map<String, Integer> definedPerSlug = new HashMap<>();
		for (CharmSlotTypes.CharmSlotType slot : defined) {
			definedRank.put(slot.id().toString(), definedPerSlug.merge(slot.def().slotType(), 1, Integer::sum));
		}
		final Map<String, Integer> occupiedDefined = new HashMap<>();
		final Map<String, Integer> occupiedGrants = new HashMap<>();
		for (Map.Entry<String, ItemStack> entry : CharmAttachments.wornCharms(player).entrySet()) {
			ItemStack stack = entry.getValue();
			if (stack.isEmpty() || !(stack.getItem() instanceof CharmItem charm)) {
				continue;
			}
			String slug = charm.slotType();
			Integer rank = definedRank.get(entry.getKey());
			if (rank != null) {
				occupiedDefined.merge(slug, rank, Math::max);
				continue;
			}
			String prefix = Tinkerer.id(slug + "_grant_").toString();
			if (entry.getKey().startsWith(prefix)) {
				String tail = entry.getKey().substring(prefix.length());
				if (!tail.isEmpty() && tail.chars().allMatch(Character::isDigit)) {
					occupiedGrants.merge(slug, Integer.parseInt(tail), Math::max);
				}
			}
		}
		final Map<String, Integer> emittedPerSlug = new HashMap<>();
		final Map<String, Integer> grantsPerSlug = new HashMap<>();
		for (String slug : definedPerSlug.keySet()) {
			int definedCount = definedPerSlug.get(slug);
			int acquiredCount = acquiredFor(player, slug, acquired, definedCount);
			emittedPerSlug.put(slug, Math.max(occupiedDefined.getOrDefault(slug, 0),
					Math.min(definedCount, acquiredCount)));
			int grantBudget = capacities == null || !capacities.containsKey(slug)
					? 0
					: Math.max(0, capacities.get(slug) - definedCount);
			grantsPerSlug.put(slug, Math.max(occupiedGrants.getOrDefault(slug, 0), grantBudget));
		}
		final List<Well> wells = new ArrayList<>();
		final Map<String, Integer> emittedSoFar = new HashMap<>();
		for (CharmSlotTypes.CharmSlotType slot : defined) {
			String slug = slot.def().slotType();
			if (emittedSoFar.merge(slug, 1, Integer::sum) <= emittedPerSlug.get(slug)) {
				wells.add(new Well(slot.id(), slot, slug, false));
			}
			if (emittedSoFar.get(slug) == definedPerSlug.get(slug)) {
				for (int n = 1; n <= grantsPerSlug.get(slug); n++) {
					wells.add(new Well(Tinkerer.id(slug + "_grant_" + n), null, slug, true));
				}
			}
		}
		return List.copyOf(wells);
	}

	public static int acquiredFor(final Player player, final String slug,
	                               final Map<String, Integer> acquired, final int definedCount) {
		if (acquired != null && acquired.containsKey(slug)) {
			return Math.min(definedCount, Math.max(0, acquired.get(slug)));
		}
		return Math.min(definedCount, CharmSlotAttributes.acquiredValue(player, slug));
	}

	public static int wellY(final int index) {
		return TOP_CAP + index * WELL_PITCH;
	}

	public static int panelHeight(final int wells) {
		if (wells <= 0) {
			return 0;
		}
		return TOP_CAP + (wells - 1) * WELL_PITCH + FOOTER_UNIT;
	}
}
