package com.aura.armory;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

public final class ModItems {
	private ModItems() {
	}

	public static final ResourceKey<Item> IRON_KATANA_KEY =
			ResourceKey.create(Registries.ITEM, Armory.id("iron_katana"));

	public static final Item IRON_KATANA = Registry.register(
			BuiltInRegistries.ITEM,
			IRON_KATANA_KEY,
			new Item(new Item.Properties().sword(ToolMaterial.IRON, 3.0F, -2.4F).setId(IRON_KATANA_KEY)));

	public static final ResourceKey<Item> WOODEN_SCYTHE_KEY =
			ResourceKey.create(Registries.ITEM, Armory.id("wooden_scythe"));
	public static final Item WOODEN_SCYTHE = Registry.register(
			BuiltInRegistries.ITEM,
			WOODEN_SCYTHE_KEY,
			new ScytheItem(ToolMaterial.WOOD, 6.0F, -3.2F, new Item.Properties().setId(WOODEN_SCYTHE_KEY)));

	public static final ResourceKey<Item> STONE_SCYTHE_KEY =
			ResourceKey.create(Registries.ITEM, Armory.id("stone_scythe"));
	public static final Item STONE_SCYTHE = Registry.register(
			BuiltInRegistries.ITEM,
			STONE_SCYTHE_KEY,
			new ScytheItem(ToolMaterial.STONE, 7.0F, -3.2F, new Item.Properties().setId(STONE_SCYTHE_KEY)));

	public static final ResourceKey<Item> COPPER_SCYTHE_KEY =
			ResourceKey.create(Registries.ITEM, Armory.id("copper_scythe"));
	public static final Item COPPER_SCYTHE = Registry.register(
			BuiltInRegistries.ITEM,
			COPPER_SCYTHE_KEY,
			new ScytheItem(ToolMaterial.COPPER, 7.0F, -3.2F, new Item.Properties().setId(COPPER_SCYTHE_KEY)));

	public static final ResourceKey<Item> IRON_SCYTHE_KEY =
			ResourceKey.create(Registries.ITEM, Armory.id("iron_scythe"));
	public static final Item IRON_SCYTHE = Registry.register(
			BuiltInRegistries.ITEM,
			IRON_SCYTHE_KEY,
			new ScytheItem(ToolMaterial.IRON, 6.0F, -3.1F, new Item.Properties().setId(IRON_SCYTHE_KEY)));

	public static final ResourceKey<Item> GOLDEN_SCYTHE_KEY =
			ResourceKey.create(Registries.ITEM, Armory.id("golden_scythe"));
	public static final Item GOLDEN_SCYTHE = Registry.register(
			BuiltInRegistries.ITEM,
			GOLDEN_SCYTHE_KEY,
			new ScytheItem(ToolMaterial.GOLD, 6.0F, -3.0F, new Item.Properties().setId(GOLDEN_SCYTHE_KEY)));

	public static final ResourceKey<Item> DIAMOND_SCYTHE_KEY =
			ResourceKey.create(Registries.ITEM, Armory.id("diamond_scythe"));
	public static final Item DIAMOND_SCYTHE = Registry.register(
			BuiltInRegistries.ITEM,
			DIAMOND_SCYTHE_KEY,
			new ScytheItem(ToolMaterial.DIAMOND, 5.0F, -3.0F, new Item.Properties().setId(DIAMOND_SCYTHE_KEY)));

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
		CreativeModeTabEvents.modifyOutputEvent(COMBAT_TAB).register((output) -> {
			output.accept(IRON_KATANA);
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
