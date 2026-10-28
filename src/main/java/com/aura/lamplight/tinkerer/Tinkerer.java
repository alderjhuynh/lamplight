package com.aura.lamplight.tinkerer;

import com.aura.lamplight.tinkerer.charm.CharmEffectTypes;
import com.aura.lamplight.tinkerer.charm.CharmDataSyncer;
import com.aura.lamplight.tinkerer.charm.CharmEvents;
import com.aura.lamplight.tinkerer.charm.CharmMaterials;
import com.aura.lamplight.tinkerer.charm.CharmSlotTypes;
import com.aura.lamplight.tinkerer.charm.CharmTemplates;
import com.aura.lamplight.tinkerer.charm.ModDataComponents;
import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tinkerer implements ModInitializer {
	public static final String MOD_ID = "lamplight";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		CharmEffectTypes.initialize();
		ModDataComponents.initialize();
		CharmMaterials.initialize();
		CharmTemplates.initialize();
		CharmSlotTypes.initialize();
		ModBlocks.register();
		ModMenus.register();
		ModItems.register();
		JewelingSetPatternPayload.initialize();
		JewelingPatternAppliedPayload.initialize();
		CharmEvents.initialize();
		CharmDataSyncer.initialize();
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
