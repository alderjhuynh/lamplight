package com.aura.lamplight.wayfarer;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public final class ModItems {
    private ModItems() {
    }

    public static final ResourceKey<Item> LEATHER_BACKPACK_KEY =
        ResourceKey.create(Registries.ITEM, Wayfarer.id("leather_backpack"));

    public static final Item LEATHER_BACKPACK = Registry.register(
        BuiltInRegistries.ITEM,
        LEATHER_BACKPACK_KEY,
        new BackpackItem(new Item.Properties().stacksTo(1).setId(LEATHER_BACKPACK_KEY)));

    public static void register() {
        Wayfarer.LOGGER.info("Registered wayfarer items");
    }
}
