package com.aura.lamplight.tinkerer.client;

import com.aura.lamplight.tinkerer.ModMenus;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;

public class TinkererClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MenuScreens.register(ModMenus.JEWELING_TABLE, CraftingScreen::new);
	}
}
