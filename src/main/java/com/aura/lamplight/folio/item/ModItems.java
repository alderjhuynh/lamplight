package com.aura.lamplight.folio.item;

import com.aura.lamplight.folio.Folio;
import com.aura.lamplight.folio.spell.SpellSchool;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public final class ModItems {
    private ModItems() {}

    public static final int SCROLL_MAX_STACK_SIZE = 16;

    public static final Item BLANK_SCROLL = register("blank_scroll", Item::new, new Item.Properties());
    public static final Item ICE_SCROLL = register("ice_scroll", p -> new ScrollItem(p, SpellSchool.ICE), new Item.Properties().stacksTo(SCROLL_MAX_STACK_SIZE));
    public static final Item ARCANE_SCROLL = register("arcane_scroll", p -> new ScrollItem(p, SpellSchool.ARCANE), new Item.Properties().stacksTo(SCROLL_MAX_STACK_SIZE));
    public static final Item BLOOD_SCROLL = register("blood_scroll", p -> new ScrollItem(p, SpellSchool.BLOOD), new Item.Properties().stacksTo(SCROLL_MAX_STACK_SIZE));
    public static final Item FLAME_SCROLL = register("flame_scroll", p -> new ScrollItem(p, SpellSchool.FLAME), new Item.Properties().stacksTo(SCROLL_MAX_STACK_SIZE));
    public static final Item LIGHTNING_SCROLL = register("lightning_scroll", p -> new ScrollItem(p, SpellSchool.LIGHTNING), new Item.Properties().stacksTo(SCROLL_MAX_STACK_SIZE));
    public static final Item NATURE_SCROLL = register("nature_scroll", p -> new ScrollItem(p, SpellSchool.NATURE), new Item.Properties().stacksTo(SCROLL_MAX_STACK_SIZE));
    public static final Item WIND_SCROLL = register("wind_scroll", p -> new ScrollItem(p, SpellSchool.WIND), new Item.Properties().stacksTo(SCROLL_MAX_STACK_SIZE));
    public static final Item HOLY_SCROLL = register("holy_scroll", p -> new ScrollItem(p, SpellSchool.HOLY), new Item.Properties().stacksTo(SCROLL_MAX_STACK_SIZE));
    public static final Item APPRENTICES_SPELLBOOK = register("apprentices_spellbook", SpellbookItem::new, new Item.Properties().stacksTo(1));

    private static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties properties) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Folio.MOD_ID, name));
        return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(properties.setId(key)));
    }

    public static void initialize() {
        Folio.LOGGER.info("Registering {} items for {}", 10, Folio.MOD_ID);
    }
}
