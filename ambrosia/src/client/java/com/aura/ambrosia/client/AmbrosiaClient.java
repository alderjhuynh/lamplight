package com.aura.ambrosia.client;

import com.aura.ambrosia.client.appleskin.AppleSkin;
import com.aura.ambrosia.client.screen.CookingPotScreen;
import com.aura.ambrosia.menu.ModMenus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.gui.screens.MenuScreens;

public class AmbrosiaClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(AppleSkin.INSTANCE::tick);
		MenuScreens.register(ModMenus.COOKING_POT, CookingPotScreen::new);
	}
}
