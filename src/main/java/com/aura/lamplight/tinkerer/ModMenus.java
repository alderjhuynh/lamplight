package com.aura.lamplight.tinkerer;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;

public class ModMenus {
    private ModMenus() {
    }

    public static final ExtendedMenuType<JewelingTableMenu, BlockPos> JEWELING_TABLE = Registry.register(
        BuiltInRegistries.MENU,
        Tinkerer.id("jeweling_table"),
        new ExtendedMenuType<>(
                (containerId, inventory, pos) -> new JewelingTableMenu(containerId, inventory, pos),
                BlockPos.STREAM_CODEC));

    public static void register() {
        Tinkerer.LOGGER.info("Registered tinkerer menus");
    }
}
