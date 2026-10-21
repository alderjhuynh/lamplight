package com.aura.lamplight.tinkerer;

import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public class ModItems {
    private ModItems(){

    }

    public static final ResourceKey<Item> JEWELING_TABLE_KEY =
        ResourceKey.create(Registries.ITEM, Tinkerer.id("jeweling_table"));

    public static final Item JEWELING_TABLE = Registry.register(
        BuiltInRegistries.ITEM,
        JEWELING_TABLE_KEY,
        new BlockItem(ModBlocks.JEWELING_TABLE, new Item.Properties().setId(JEWELING_TABLE_KEY)));

    public static final ResourceKey<Item> ROSE_GOLD_INGOT_KEY =
        ResourceKey.create(Registries.ITEM, Tinkerer.id("rose_gold_ingot"));

    public static final Item ROSE_GOLD_INGOT = Registry.register(
        BuiltInRegistries.ITEM,
        ROSE_GOLD_INGOT_KEY,
        new Item(new Item.Properties().fireResistant().setId(ROSE_GOLD_INGOT_KEY)));

    public static final ResourceKey<Item> ROSE_GOLD_NUGGET_KEY =
        ResourceKey.create(Registries.ITEM, Tinkerer.id("rose_gold_nugget"));

    public static final Item ROSE_GOLD_NUGGET = Registry.register(
        BuiltInRegistries.ITEM,
        ROSE_GOLD_NUGGET_KEY,
        new Item(new Item.Properties().fireResistant().setId(ROSE_GOLD_NUGGET_KEY)));

    public static final ResourceKey<Item> RAW_ROSE_GOLD_KEY =
        ResourceKey.create(Registries.ITEM, Tinkerer.id("raw_rose_gold"));

    public static final Item RAW_ROSE_GOLD = Registry.register(
        BuiltInRegistries.ITEM,
        RAW_ROSE_GOLD_KEY,
        new Item(new Item.Properties().fireResistant().setId(RAW_ROSE_GOLD_KEY)));

    public static void register() {
        // TODO: ADD CREATIVE TAB
        // CreativeModeTabEvents.modifyOutputEvent(null)
        Tinkerer.LOGGER.info("Registered tinkerer items");
    }
}
