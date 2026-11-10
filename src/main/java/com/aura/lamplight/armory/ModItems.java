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

	public static final ResourceKey<Item> IRON_KATANA_KEY =
			ResourceKey.create(Registries.ITEM, Armory.id("iron_katana"));

	public static final Item IRON_KATANA = Registry.register(
			BuiltInRegistries.ITEM,
			IRON_KATANA_KEY,
			new Item(new Item.Properties().sword(ToolMaterial.IRON, 3.0F, -3.0F).setId(IRON_KATANA_KEY)));

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
			for (Item scythe : new Item[] {
					WOODEN_SCYTHE, STONE_SCYTHE, COPPER_SCYTHE, IRON_SCYTHE,
					GOLDEN_SCYTHE, DIAMOND_SCYTHE, NETHERITE_SCYTHE }) {
				addScytheAttributes(context, scythe);
			}
		});
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
