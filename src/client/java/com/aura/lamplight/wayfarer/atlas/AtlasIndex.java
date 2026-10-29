package com.aura.lamplight.wayfarer.atlas;

import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.level.Level;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class AtlasIndex {

	private record Snapshot(Level level, Object source, int stamp, AtlasIndex index) {
		boolean matches(final Level level, final Object source, final int stamp) {
			return this.level == level && this.source == source && this.stamp == stamp;
		}
	}

	private static volatile @Nullable Snapshot cached;

	/** Drops the cached index; called when the server pushes a new recipe set. */
	public static void invalidate() {
		cached = null;
	}

	public static AtlasIndex get(final Minecraft minecraft, final boolean force) {
		Level level = minecraft.level;
		List<RecipeDisplay> synced = AtlasClient.synced();
		Object source;
		int stamp;
		if (level == null) {
			source = null;
			stamp = 0;
		} else if (!synced.isEmpty()) {
			source = synced;
			stamp = AtlasClient.revision();
		} else {
			ClientRecipeBook book = minecraft.player == null ? null : minecraft.player.getRecipeBook();
			source = book;
			stamp = book == null ? 0 : totalEntries(book);
		}
		Snapshot snapshot = cached;
		if (!force && snapshot != null && snapshot.matches(level, source, stamp)) {
			return snapshot.index;
		}
		AtlasIndex index;
		if (level == null || source == null) {
			index = new AtlasIndex(Map.of(), Map.of(), List.of());
		} else {
			ContextMap context = SlotDisplayContext.fromLevel(level);
			if (source instanceof List) {
				@SuppressWarnings("unchecked")
				List<RecipeDisplay> displays = (List<RecipeDisplay>) source;
				index = build(level, context, entryDisplays(displays, context));
			} else {
				ClientRecipeBook book = (ClientRecipeBook) source;
				index = build(level, context, bookEntries(book, context));
			}
		}
		cached = new Snapshot(level, source, stamp, index);
		return index;
	}

	private static List<AtlasEntry> entryDisplays(final List<RecipeDisplay> displays, final ContextMap context) {
		List<AtlasEntry> entries = new ArrayList<>();
		for (RecipeDisplay display : displays) {
			AtlasEntry entry = AtlasEntry.fromDisplay(display, context);
			if (entry != null) {
				entries.add(entry);
			}
		}
		return entries;
	}

	private static List<AtlasEntry> bookEntries(final ClientRecipeBook book, final ContextMap context) {
		List<AtlasEntry> entries = new ArrayList<>();
		for (net.minecraft.client.gui.screens.recipebook.RecipeCollection collection : book.getCollections()) {
			for (RecipeDisplayEntry display : collection.getRecipes()) {
				AtlasEntry entry = AtlasEntry.from(display, context);
				if (entry != null) {
					entries.add(entry);
				}
			}
		}
		return entries;
	}

	private static int totalEntries(final ClientRecipeBook book) {
		int total = 0;
		for (net.minecraft.client.gui.screens.recipebook.RecipeCollection collection : book.getCollections()) {
			total += collection.getRecipes().size();
		}
		return total;
	}

	private AtlasIndex(final Map<Item, List<AtlasEntry>> results,
	                   final Map<Item, List<AtlasEntry>> usages, final List<Item> items) {
		this.results = results;
		this.usages = usages;
		this.items = items;
	}

	private final Map<Item, List<AtlasEntry>> results;
	private final Map<Item, List<AtlasEntry>> usages;
	private final List<Item> items;

	public List<Item> items() {
		return this.items;
	}

	public List<AtlasEntry> resultsFor(final Item item) {
		return this.results.getOrDefault(item, List.of());
	}

	public List<AtlasEntry> usagesFor(final Item item) {
		return this.usages.getOrDefault(item, List.of());
	}

	private static AtlasIndex build(final Level level, final ContextMap context, final List<AtlasEntry> entries) {
		Map<Item, List<AtlasEntry>> results = new LinkedHashMap<>();
		Map<Item, List<AtlasEntry>> usages = new LinkedHashMap<>();
		Set<Item> itemsSeen = new HashSet<>();
		for (AtlasEntry entry : entries) {
			for (ItemStack stack : entry.resultStacks()) {
				if (stack.isEmpty()) {
					continue;
				}
				Item item = stack.getItem();
				if (itemsSeen.add(item)) {
					results.computeIfAbsent(item, key -> new ArrayList<>());
				}
				results.get(item).add(entry);
			}
			Set<Item> slotItems = new HashSet<>();
			for (List<ItemStack> slot : entry.ingredientSlots()) {
				for (ItemStack stack : slot) {
					if (!stack.isEmpty()) {
						slotItems.add(stack.getItem());
					}
				}
			}
			for (Item item : slotItems) {
				usages.computeIfAbsent(item, key -> new ArrayList<>()).add(entry);
			}
		}
		List<Item> items = new ArrayList<>(itemsSeen);
		items.sort(Comparator.comparing(item -> BuiltInRegistries.ITEM.getKey(item).toString()));
		Map<Item, List<AtlasEntry>> immutableResults = new HashMap<>();
		results.forEach((key, value) -> immutableResults.put(key, List.copyOf(value)));
		Map<Item, List<AtlasEntry>> immutableUsages = new HashMap<>();
		usages.forEach((key, value) -> immutableUsages.put(key, List.copyOf(value)));
		return new AtlasIndex(Map.copyOf(immutableResults), Map.copyOf(immutableUsages), List.copyOf(items));
	}
}
