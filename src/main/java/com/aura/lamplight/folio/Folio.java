package com.aura.lamplight.folio;

import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aura.lamplight.folio.component.ModDataComponents;
import com.aura.lamplight.folio.entity.ModEntityTypes;
import com.aura.lamplight.folio.item.ModItems;
import com.aura.lamplight.folio.spell.Spells;

public class Folio implements ModInitializer {
	public static final String MOD_ID = "lamplight";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("folio registered");
		ModDataComponents.initialize();
		ModEntityTypes.initialize();
		Spells.initialize();
		ModItems.initialize();
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
