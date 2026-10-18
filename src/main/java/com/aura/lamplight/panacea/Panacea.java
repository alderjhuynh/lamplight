package com.aura.lamplight.panacea;

import com.aura.lamplight.panacea.registry.ModBlockEntities;
import com.aura.lamplight.panacea.registry.ModBlocks;
import com.aura.lamplight.panacea.registry.ModComponents;
import com.aura.lamplight.panacea.registry.ModEffects;
import com.aura.lamplight.panacea.registry.ModPotionRecipes;
import com.aura.lamplight.panacea.registry.ModPotions;
import com.aura.lamplight.panacea.block.PotionCauldronInteractions;
import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Panacea implements ModInitializer {
	public static final String MOD_ID = "lamplight";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		ModEffects.register();
		ModComponents.register();
		ModPotions.register();
		ModPotionRecipes.register();
		ModBlocks.register();
		ModBlockEntities.register();
		PotionCauldronInteractions.register();

		LOGGER.info("Panacea initialized!");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
