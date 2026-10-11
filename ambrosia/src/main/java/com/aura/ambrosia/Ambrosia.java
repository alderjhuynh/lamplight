package com.aura.ambrosia;

import com.aura.ambrosia.effect.ModEffects;
import com.aura.ambrosia.food.VanillaFoodEffects;
import com.aura.ambrosia.item.ModItems;
import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ambrosia implements ModInitializer {
	public static final String MOD_ID = "ambrosia";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModEffects.register();
		VanillaFoodEffects.register();
		ModItems.initialize();

		LOGGER.info("Ambrosia registered");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
