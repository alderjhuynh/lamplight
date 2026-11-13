package com.aura.lamplight.wayfarer;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;

public class ModMenus {
    private ModMenus() {
    }

    public static final ExtendedMenuType<BackpackMenu, Integer> LEATHER_BACKPACK = Registry.register(
        BuiltInRegistries.MENU,
        Wayfarer.id("leather_backpack"),
        new ExtendedMenuType<>(BackpackMenu::new, ByteBufCodecs.VAR_INT));

    public static void register() {
        Wayfarer.LOGGER.info("Registered wayfarer menus");
    }
}
