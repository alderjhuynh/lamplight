package com.aura.lamplight.ambrosia;

import com.aura.lamplight.ambrosia.block.ModBlockEntities;
import com.aura.lamplight.ambrosia.block.ModBlocks;
import com.aura.lamplight.ambrosia.effect.ModEffects;
import com.aura.lamplight.ambrosia.food.ModFoods;
import com.aura.lamplight.ambrosia.food.VanillaFoodEffects;
import com.aura.lamplight.ambrosia.item.ModCreativeTab;
import com.aura.lamplight.ambrosia.item.ModItems;
import com.aura.lamplight.ambrosia.item.ModLoot;
import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ambrosia implements ModInitializer {
	public static final String MOD_ID = "lamplight";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModEffects.register();
		VanillaFoodEffects.register();
		ModBlocks.register();
		ModBlockEntities.register();
		ModItems.initialize();
		ModCreativeTab.register();
		ModFoods.register();
		ModLoot.register();

		LOGGER.info("Ambrosia registered");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
