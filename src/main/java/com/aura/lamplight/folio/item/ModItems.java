package com.aura.lamplight.folio.item;

import com.aura.lamplight.folio.Folio;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public final class ModItems {
    private ModItems() {}

    /** Ice scrolls stack to 16, but only with other scrolls carrying the exact same spell. */
    public static final int ICE_SCROLL_MAX_STACK_SIZE = 16;

    public static final Item BLANK_SCROLL = register("blank_scroll", Item::new, new Item.Properties());
    public static final Item ROLLED_SCROLL = register("rolled_scroll", Item::new, new Item.Properties());
    public static final Item ICE_SCROLL = register("ice_scroll", IceScrollItem::new, new Item.Properties().stacksTo(ICE_SCROLL_MAX_STACK_SIZE));
    public static final Item APPRENTICES_SPELLBOOK = register("apprentices_spellbook", Item::new, new Item.Properties().stacksTo(1));

    private static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties properties) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Folio.MOD_ID, name));
        return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(properties.setId(key)));
    }

    public static void initialize() {
        Folio.LOGGER.info("Registering {} items for {}", 4, Folio.MOD_ID);
    }
}
