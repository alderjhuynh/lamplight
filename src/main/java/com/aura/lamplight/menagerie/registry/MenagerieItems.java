package com.aura.lamplight.menagerie.registry;

import com.aura.lamplight.menagerie.Menagerie;
import com.aura.lamplight.menagerie.fishing.MessageInABottleItem;
import java.util.function.Function;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

public final class MenagerieItems {
    private MenagerieItems() {}

    public static final Item MESSAGE_IN_A_BOTTLE = register("message_in_a_bottle", MessageInABottleItem::new,
            new Item.Properties()
                    .stacksTo(1)
                    .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));

    private static final ResourceKey<CreativeModeTab> TOOLS_TAB =
            ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.withDefaultNamespace("tools"));

    private static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties properties) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Menagerie.MOD_ID, name));
        return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(properties.setId(key)));
    }

    public static void init() {
        CreativeModeTabEvents.modifyOutputEvent(TOOLS_TAB).register(output -> output.accept(MESSAGE_IN_A_BOTTLE));
        Menagerie.LOGGER.info("Registering {} items for {}", 1, Menagerie.MOD_ID);
    }
}
