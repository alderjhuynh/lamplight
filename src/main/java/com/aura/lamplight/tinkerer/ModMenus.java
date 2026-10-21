package com.aura.lamplight.tinkerer;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.flag.FeatureFlags;

public class ModMenus {
    private ModMenus() {
    }

    public static final MenuType<JewelingTableMenu> JEWELING_TABLE = Registry.register(
        BuiltInRegistries.MENU,
        Tinkerer.id("jeweling_table"),
        new MenuType<>(JewelingTableMenu::new, FeatureFlags.VANILLA_SET));

    public static void register() {
        Tinkerer.LOGGER.info("Registered tinkerer menus");
    }
}
