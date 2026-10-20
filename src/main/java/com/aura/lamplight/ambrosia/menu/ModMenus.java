package com.aura.lamplight.ambrosia.menu;

import com.aura.lamplight.ambrosia.Ambrosia;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public final class ModMenus {
    private ModMenus() {}

    public static final ExtendedMenuType<CookingPotMenu, BlockPos> COOKING_POT = Registry.register(
            BuiltInRegistries.MENU, Ambrosia.id("cooking_pot"),
            new ExtendedMenuType<>((containerId, inventory, pos) -> new CookingPotMenu(containerId, inventory, pos),
                    BlockPos.STREAM_CODEC));

    public static void register() {
        Ambrosia.LOGGER.info("Registering menus for {}", Ambrosia.MOD_ID);
    }
}
