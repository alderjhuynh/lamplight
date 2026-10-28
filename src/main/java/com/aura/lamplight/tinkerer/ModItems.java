package com.aura.lamplight.tinkerer;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public class ModItems {
    private ModItems() {

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

    public static final ResourceKey<Item> RUBY_KEY =
        ResourceKey.create(Registries.ITEM, Tinkerer.id("ruby"));

    public static final Item RUBY = Registry.register(
        BuiltInRegistries.ITEM,
        RUBY_KEY,
        new Item(new Item.Properties().setId(RUBY_KEY)));

    public static final ResourceKey<Item> SAPPHIRE_KEY =
        ResourceKey.create(Registries.ITEM, Tinkerer.id("sapphire"));

    public static final Item SAPPHIRE = Registry.register(
        BuiltInRegistries.ITEM,
        SAPPHIRE_KEY,
        new Item(new Item.Properties().setId(SAPPHIRE_KEY)));

    public static final String[] CHARM_TYPE_NAMES = {"ring", "necklace", "cuff"};

    public static final String[] TEMPLATE_NAMES = {
            "band", "twin_band", "signet", "locket", "pendant", "collar", "cuff"};

    public static final Map<String, TemplateItem> TEMPLATES = new LinkedHashMap<>();

    public static final Map<String, CharmItem> CHARMS = new LinkedHashMap<>();

    private static void registerTemplates() {
        for (String name : TEMPLATE_NAMES) {
            ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Tinkerer.id("template_" + name));
            TEMPLATES.put(name, Registry.register(BuiltInRegistries.ITEM, key,
                    new TemplateItem(new Item.Properties().fireResistant().stacksTo(16).setId(key))));
        }
    }

    private static void registerCharms() {
        for (String family : CHARM_TYPE_NAMES) {
            ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Tinkerer.id("charm_" + family));
            CHARMS.put(family, Registry.register(BuiltInRegistries.ITEM, key,
                    new CharmItem(family, new Item.Properties().fireResistant().stacksTo(1).setId(key))));
        }
    }

    public static void register() {
        registerTemplates();
        registerCharms();
        Tinkerer.LOGGER.info("Registered tinkerer items");
    }
}
