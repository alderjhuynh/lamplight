package com.aura.lamplight.wayfarer.atlas;

import com.aura.lamplight.tinkerer.ModBlocks;
import com.aura.lamplight.tinkerer.CharmAssembler;
import com.aura.lamplight.tinkerer.JewelingTableMenu;
import com.aura.lamplight.tinkerer.charm.CharmMaterial;
import com.aura.lamplight.tinkerer.charm.CharmMaterials;
import com.aura.lamplight.tinkerer.charm.CharmTemplate;
import com.aura.lamplight.tinkerer.charm.CharmTemplates;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.FurnaceRecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SmithingRecipeDisplay;
import net.minecraft.world.item.crafting.display.StonecutterRecipeDisplay;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public sealed interface AtlasEntry permits AtlasEntry.RecipeEntry, AtlasEntry.JewelingEntry {

	enum Kind { SHAPED, SHAPELESS, FURNACE, SMITHING, STONECUTTER, JEWELING, OTHER }

	Kind kind();

	List<ItemStack> resultStacks();

	List<List<ItemStack>> ingredientSlots();

	default @Nullable ItemStack stationIcon() {
		return null;
	}

	default List<ItemStack> resultItems() {
		return this.resultStacks();
	}

	static List<ItemStack> resolve(final SlotDisplay display, final ContextMap context) {
		List<ItemStack> stacks = new ArrayList<>();
		for (ItemStack stack : display.resolveForStacks(context)) {
			if (!stack.isEmpty()) {
				stacks.add(stack);
			}
		}
		return List.copyOf(stacks);
	}


	static @Nullable AtlasEntry from(final RecipeDisplayEntry entry, final ContextMap context) {
		RecipeEntry built = buildDisplay(entry.display(), context);
		if (built == null) {
			return null;
		}
		if (built.kind() == Kind.OTHER) {
			List<List<ItemStack>> requirementSlots = fromRequirements(entry.craftingRequirements());
			if (!requirementSlots.isEmpty()) {
				built = new RecipeEntry(Kind.OTHER, built.resultStacks(), requirementSlots, 0, 0, 0, 0.0F,
						built.station());
			}
		}
		return built;
	}

	static @Nullable AtlasEntry fromDisplay(final RecipeDisplay display, final ContextMap context) {
		return buildDisplay(display, context);
	}

	private static @Nullable RecipeEntry buildDisplay(final RecipeDisplay display, final ContextMap context) {
		ItemStack station = firstStack(display.craftingStation(), context);
		RecipeEntry built;
		if (display instanceof ShapedCraftingRecipeDisplay shaped) {
			List<List<ItemStack>> slots = new ArrayList<>(shaped.width() * shaped.height());
			for (SlotDisplay slot : shaped.ingredients()) {
				slots.add(AtlasEntry.resolve(slot, context));
			}
			while (slots.size() < shaped.width() * shaped.height()) {
				slots.add(List.of());
			}
			built = new RecipeEntry(Kind.SHAPED, resolve(shaped.result(), context), List.copyOf(slots),
					shaped.width(), shaped.height(), 0, 0.0F, station);
		} else if (display instanceof ShapelessCraftingRecipeDisplay shapeless) {
			List<List<ItemStack>> slots = new ArrayList<>();
			for (SlotDisplay slot : shapeless.ingredients()) {
				slots.add(AtlasEntry.resolve(slot, context));
			}
			built = new RecipeEntry(Kind.SHAPELESS, resolve(shapeless.result(), context),
					List.copyOf(slots), 0, 0, 0, 0.0F, station);
		} else if (display instanceof FurnaceRecipeDisplay furnace) {
			List<List<ItemStack>> slots = List.of(resolve(furnace.ingredient(), context),
					resolve(furnace.fuel(), context));
			built = new RecipeEntry(Kind.FURNACE, resolve(furnace.result(), context), slots, 0, 0,
					furnace.duration(), furnace.experience(), station);
		} else if (display instanceof SmithingRecipeDisplay smithing) {
			List<List<ItemStack>> slots = List.of(resolve(smithing.template(), context),
					resolve(smithing.base(), context), resolve(smithing.addition(), context));
			built = new RecipeEntry(Kind.SMITHING, resolve(smithing.result(), context), slots, 0, 0, 0, 0.0F, station);
		} else if (display instanceof StonecutterRecipeDisplay cutting) {
			List<List<ItemStack>> slots = List.of(resolve(cutting.input(), context));
			built = new RecipeEntry(Kind.STONECUTTER, resolve(cutting.result(), context), slots, 0, 0, 0, 0.0F, station);
		} else {
			built = new RecipeEntry(Kind.OTHER, resolve(display.result(), context), List.of(), 0, 0, 0, 0.0F, station);
		}
		if (built.resultStacks().isEmpty()) {
			return null;
		}
		return built;
	}

	private static @Nullable ItemStack firstStack(final SlotDisplay display, final ContextMap context) {
		for (ItemStack stack : display.resolveForStacks(context)) {
			if (!stack.isEmpty()) {
				return stack;
			}
		}
		return null;
	}

	@SuppressWarnings ("deprecation")
	private static List<List<ItemStack>> fromRequirements(final Optional<List<Ingredient>> requirements) {
		if (requirements.isEmpty()) {
			return List.of();
		}
		List<List<ItemStack>> slots = new ArrayList<>();
		for (Ingredient ingredient : requirements.get()) {
			List<ItemStack> stacks = new ArrayList<>();
			ingredient.items().forEach(item ->
					stacks.add(new ItemStack(item.value())));
			slots.add(List.copyOf(stacks));
		}
		return List.copyOf(slots);
	}

	record RecipeEntry(Kind kind, List<ItemStack> resultStacks, List<List<ItemStack>> ingredientSlots,
	                   int gridWidth, int gridHeight, int duration, float experience,
	                   @Nullable ItemStack station) implements AtlasEntry {
	}

	final class JewelingEntry implements AtlasEntry {

		private final CharmTemplates.TemplateEntry template;
		private int settingOrdinal;
		private int insetOrdinal;

		public JewelingEntry(final CharmTemplates.TemplateEntry template) {
			this.template = template;
		}

		public CharmTemplates.TemplateEntry template() {
			return this.template;
		}

		public CharmTemplate def() {
			return this.template.def();
		}

		public void cycleSetting() {
			this.settingOrdinal++;
		}

		public void cycleInset() {
			this.insetOrdinal++;
		}

		public CharmMaterial settingMaterial() {
			return pick(this.settingOrdinal);
		}

		public CharmMaterial insetMaterial() {
			return pick(this.insetOrdinal);
		}

		private static CharmMaterial pick(final int ordinal) {
			List<CharmMaterial> materials = CharmMaterials.orderedMaterials();
			return materials.get(Math.floorMod(ordinal, materials.size()));
		}

		public boolean available(final Player player) {
			return CharmTemplates.isAvailable(player, this.def());
		}

		public @Nullable Item moldItem() {
			return BuiltInRegistries.ITEM.getOptional(this.def().item()).orElse(null);
		}

		public int insetCells() {
			return Math.min(this.def().insetCount(), JewelingTableMenu.MAX_INSETS);
		}

		public int insetRowSpan() {
			return (1 + this.insetCells() + 3) / 4;
		}

		public int settingCost() {
			return this.settingMaterial().settingCost();
		}

		public int insetCost() {
			return this.insetMaterial().insetCost();
		}

		@Override
		public Kind kind() {
			return Kind.JEWELING;
		}

		@Override
		public @Nullable ItemStack stationIcon() {
			return new ItemStack(ModBlocks.JEWELING_TABLE);
		}

		@Override
		public List<ItemStack> resultStacks() {
			ItemStack stack = CharmAssembler.buildResult(this.def(), this.settingMaterial(),
					Collections.nCopies(this.insetCells(), this.insetMaterial()));
			return stack.isEmpty() ? List.of() : List.of(stack);
		}

		@Override
		public List<List<ItemStack>> ingredientSlots() {
			List<List<ItemStack>> slots = new ArrayList<>(1 + this.insetCells());
			slots.add(materialSlot(this.settingMaterial()));
			for (int i = 0; i < this.insetCells(); i++) {
				slots.add(materialSlot(this.insetMaterial()));
			}
			return List.copyOf(slots);
		}

		private static List<ItemStack> materialSlot(final CharmMaterial material) {
			Item item = BuiltInRegistries.ITEM.getOptional(material.item()).orElse(null);
			return item == null ? List.of() : List.of(new ItemStack(item));
		}
	}
}
