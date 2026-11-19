package com.aura.lamplight.folio.menu;

import com.aura.lamplight.folio.Folio;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;

public final class FolioMenus {
    private FolioMenus() {}

    public static final ExtendedMenuType<SpellbookMenu, Integer> SPELLBOOK = Registry.register(
        BuiltInRegistries.MENU,
        Folio.id("spellbook"),
        new ExtendedMenuType<>(
            (containerId, inventory, ignored) -> new SpellbookMenu(containerId, inventory),
            ByteBufCodecs.VAR_INT
        )
    );

    public static void initialize() {
        Folio.LOGGER.info("Registered folio menus");
    }
}
