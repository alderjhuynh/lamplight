package com.aura.ambrosia.item;

import com.aura.ambrosia.Ambrosia;
import java.util.function.Function;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

public final class ModItems {
    private ModItems() {}

    public static final Item FRYING_PAN = register("frying_pan", FryingPanItem::new, new Item.Properties().stacksTo(1));

    private static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties properties) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Ambrosia.MOD_ID, name));
        return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(properties.setId(key)));
    }

    private static final ResourceKey<CreativeModeTab> TOOLS_TAB =
            ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.withDefaultNamespace("tools"));

    public static void initialize() {
        CreativeModeTabEvents.modifyOutputEvent(TOOLS_TAB).register((output) -> output.accept(FRYING_PAN));
        Ambrosia.LOGGER.info("Registering {} items for {}", 1, Ambrosia.MOD_ID);
    }
}
