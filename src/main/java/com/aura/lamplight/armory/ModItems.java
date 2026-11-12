package com.aura.lamplight.armory;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public final class ModItems {
	private ModItems() {
	}

	private static void addScytheAttributes(DefaultItemComponentEvents.ModifyContext context, Item scythe) {
		context.modify(scythe, builder -> {
			ItemAttributeModifiers existing = builder.getOrDefault(
					DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
			builder.set(DataComponents.ATTRIBUTE_MODIFIERS, existing
					.withModifierAdded(
							Attributes.ENTITY_INTERACTION_RANGE,
							new AttributeModifier(Armory.id("scythe_reach"), 1.0D,
									AttributeModifier.Operation.ADD_VALUE),
							EquipmentSlotGroup.MAINHAND)
					.withModifierAdded(
							Attributes.SWEEPING_DAMAGE_RATIO,
							new AttributeModifier(Armory.id("scythe_sweep"), 1.0D,
									AttributeModifier.Operation.ADD_VALUE),
							EquipmentSlotGroup.MAINHAND));
		});
	}

	private static void addTwoHanded(DefaultItemComponentEvents.ModifyContext context, Item item) {
		context.modify(item, builder -> {
			builder.set(ModDataComponents.TWO_HANDED, true);
			Armory.LOGGER.info("[twohanded-debug] default component bound to {}", item);
		});
	}

	private static void addDualWielded(DefaultItemComponentEvents.ModifyContext context, Item item) {
		context.modify(item, builder -> builder.set(ModDataComponents.DUAL_WIELDED, true));
	}

	public static final ResourceKey<Item> IRON_KATANA_KEY =
			ResourceKey.create(Registries.ITEM, Armory.id("iron_katana"));

	public static final Item IRON_KATANA = Registry.register(
			BuiltInRegistries.ITEM,
			IRON_KATANA_KEY,
			new Item(new Item.Properties().sword(ToolMaterial.IRON, 3.0F, -3.0F).setId(IRON_KATANA_KEY)));

	public static final ResourceKey<Item> NETHERITE_GREATSWORD_KEY =
			ResourceKey.create(Registries.ITEM, Armory.id("netherite_greatsword"));
	public static final Item NETHERITE_GREATSWORD = Registry.register(
			BuiltInRegistries.ITEM,
			NETHERITE_GREATSWORD_KEY, 
			new Item(new Item.Properties().sword(ToolMaterial.NETHERITE, 5.0F, -3.3F).setId(NETHERITE_GREATSWORD_KEY)));

	public static final ResourceKey<Item> WOODEN_GREATSWORD_KEY =
			ResourceKey.create(Registries.ITEM, Armory.id("wooden_greatsword"));
	public static final Item WOODEN_GREATSWORD = Registry.register(
			BuiltInRegistries.ITEM,
			WOODEN_GREATSWORD_KEY,
			new Item(new Item.Properties().sword(ToolMaterial.WOOD, 4.0F, -3.3F).setId(WOODEN_GREATSWORD_KEY)));

	public static final ResourceKey<Item> STONE_GREATSWORD_KEY =
			ResourceKey.create(Registries.ITEM, Armory.id("stone_greatsword"));
	public static final Item STONE_GREATSWORD = Registry.register(
			BuiltInRegistries.ITEM,
			STONE_GREATSWORD_KEY,
			new Item(new Item.Properties().sword(ToolMaterial.STONE, 4.0F, -3.3F).setId(STONE_GREATSWORD_KEY)));

	public static final ResourceKey<Item> COPPER_GREATSWORD_KEY =
			ResourceKey.create(Registries.ITEM, Armory.id("copper_greatsword"));
	public static final Item COPPER_GREATSWORD = Registry.register(
			BuiltInRegistries.ITEM,
			COPPER_GREATSWORD_KEY,
			new Item(new Item.Properties().sword(ToolMaterial.COPPER, 4.0F, -3.3F).setId(COPPER_GREATSWORD_KEY)));

	public static final ResourceKey<Item> IRON_GREATSWORD_KEY =
			ResourceKey.create(Registries.ITEM, Armory.id("iron_greatsword"));
	public static final Item IRON_GREATSWORD = Registry.register(
			BuiltInRegistries.ITEM,
			IRON_GREATSWORD_KEY,
			new Item(new Item.Properties().sword(ToolMaterial.IRON, 5.0F, -3.3F).setId(IRON_GREATSWORD_KEY)));

	public static final ResourceKey<Item> GOLDEN_GREATSWORD_KEY =
			ResourceKey.create(Registries.ITEM, Armory.id("golden_greatsword"));
	public static final Item GOLDEN_GREATSWORD = Registry.register(
			BuiltInRegistries.ITEM,
			GOLDEN_GREATSWORD_KEY,
			new Item(new Item.Properties().sword(ToolMaterial.GOLD, 4.0F, -3.3F).setId(GOLDEN_GREATSWORD_KEY)));

	public static final ResourceKey<Item> DIAMOND_GREATSWORD_KEY =
			ResourceKey.create(Registries.ITEM, Armory.id("diamond_greatsword"));
	public static final Item DIAMOND_GREATSWORD = Registry.register(
			BuiltInRegistries.ITEM,
			DIAMOND_GREATSWORD_KEY,
			new Item(new Item.Properties().sword(ToolMaterial.DIAMOND, 5.0F, -3.3F).setId(DIAMOND_GREATSWORD_KEY)));

	public static final ResourceKey<Item> WOODEN_SCYTHE_KEY =
			ResourceKey.create(Registries.ITEM, Armory.id("wooden_scythe"));
	public static final Item WOODEN_SCYTHE = Registry.register(
			BuiltInRegistries.ITEM,
			WOODEN_SCYTHE_KEY,
			new ScytheItem(ToolMaterial.WOOD, 4.0F, -3.0F, new Item.Properties().setId(WOODEN_SCYTHE_KEY)));

	public static final ResourceKey<Item> STONE_SCYTHE_KEY =
			ResourceKey.create(Registries.ITEM, Armory.id("stone_scythe"));
	public static final Item STONE_SCYTHE = Registry.register(
			BuiltInRegistries.ITEM,
			STONE_SCYTHE_KEY,
			new ScytheItem(ToolMaterial.STONE, 4.0F, -3.0F, new Item.Properties().setId(STONE_SCYTHE_KEY)));

	public static final ResourceKey<Item> COPPER_SCYTHE_KEY =
			ResourceKey.create(Registries.ITEM, Armory.id("copper_scythe"));
	public static final Item COPPER_SCYTHE = Registry.register(
			BuiltInRegistries.ITEM,
			COPPER_SCYTHE_KEY,
			new ScytheItem(ToolMaterial.COPPER, 4.0F, -3.0F, new Item.Properties().setId(COPPER_SCYTHE_KEY)));

	public static final ResourceKey<Item> IRON_SCYTHE_KEY =
			ResourceKey.create(Registries.ITEM, Armory.id("iron_scythe"));
	public static final Item IRON_SCYTHE = Registry.register(
			BuiltInRegistries.ITEM,
			IRON_SCYTHE_KEY,
			new ScytheItem(ToolMaterial.IRON, 4.0F, -3.0F, new Item.Properties().setId(IRON_SCYTHE_KEY)));

	public static final ResourceKey<Item> GOLDEN_SCYTHE_KEY =
			ResourceKey.create(Registries.ITEM, Armory.id("golden_scythe"));
	public static final Item GOLDEN_SCYTHE = Registry.register(
			BuiltInRegistries.ITEM,
			GOLDEN_SCYTHE_KEY,
			new ScytheItem(ToolMaterial.GOLD, 4.0F, -3.0F, new Item.Properties().setId(GOLDEN_SCYTHE_KEY)));

	public static final ResourceKey<Item> DIAMOND_SCYTHE_KEY =
			ResourceKey.create(Registries.ITEM, Armory.id("diamond_scythe"));
	public static final Item DIAMOND_SCYTHE = Registry.register(
			BuiltInRegistries.ITEM,
			DIAMOND_SCYTHE_KEY,
			new ScytheItem(ToolMaterial.DIAMOND, 4.0F, -3.0F, new Item.Properties().setId(DIAMOND_SCYTHE_KEY)));

	public static final ResourceKey<Item> NETHERITE_SCYTHE_KEY =
			ResourceKey.create(Registries.ITEM, Armory.id("netherite_scythe"));
	public static final Item NETHERITE_SCYTHE = Registry.register(
			BuiltInRegistries.ITEM,
			NETHERITE_SCYTHE_KEY,
			new ScytheItem(ToolMaterial.NETHERITE, 5.0F, -3.0F,
					new Item.Properties().fireResistant().setId(NETHERITE_SCYTHE_KEY)));

	public static final ResourceKey<CreativeModeTab> COMBAT_TAB =
			ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.withDefaultNamespace("combat"));

	public static void register() {
		DefaultItemComponentEvents.MODIFY.register(context -> {
			addDualWielded(context, IRON_KATANA);
			for (Item scythe : new Item[] {
					WOODEN_SCYTHE, STONE_SCYTHE, COPPER_SCYTHE, IRON_SCYTHE,
					GOLDEN_SCYTHE, DIAMOND_SCYTHE, NETHERITE_SCYTHE }) {
				addScytheAttributes(context, scythe);
			}
			for (Item item : new Item[] {
					WOODEN_GREATSWORD, STONE_GREATSWORD, COPPER_GREATSWORD, IRON_GREATSWORD,
					GOLDEN_GREATSWORD, DIAMOND_GREATSWORD, NETHERITE_GREATSWORD,
					Items.BOW, Items.CROSSBOW, Items.MACE }) {
				addTwoHanded(context, item);
			}
		});
		CreativeModeTabEvents.modifyOutputEvent(COMBAT_TAB).register((output) -> {
			output.accept(IRON_KATANA);
			output.accept(WOODEN_GREATSWORD);
			output.accept(STONE_GREATSWORD);
			output.accept(COPPER_GREATSWORD);
			output.accept(IRON_GREATSWORD);
			output.accept(GOLDEN_GREATSWORD);
			output.accept(DIAMOND_GREATSWORD);
			output.accept(NETHERITE_GREATSWORD);
			output.accept(WOODEN_SCYTHE);
			output.accept(STONE_SCYTHE);
			output.accept(COPPER_SCYTHE);
			output.accept(IRON_SCYTHE);
			output.accept(GOLDEN_SCYTHE);
			output.accept(DIAMOND_SCYTHE);
			output.accept(NETHERITE_SCYTHE);
		});
		Armory.LOGGER.info("Registered armory items");
	}
}
