package com.aura.lamplight.wayfarer;

import com.aura.lamplight.wayfarer.atlas.AtlasSyncPayload;
import com.aura.lamplight.wayfarer.waystone.ModBlocks;
import com.aura.lamplight.wayfarer.waystone.WaystoneChanneling;
import com.aura.lamplight.wayfarer.waystone.WaystoneNamePayload;
import com.aura.lamplight.wayfarer.waystone.WaystoneNamingPayload;
import com.aura.lamplight.wayfarer.waystone.WaystoneTravelListPayload;
import com.aura.lamplight.wayfarer.waystone.WaystoneTravelPayload;
import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Wayfarer implements ModInitializer {
	public static final String MOD_ID = "lamplight";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModBlocks.register();
		WaystoneTravelListPayload.initialize();
		WaystoneTravelPayload.initialize();
		WaystoneNamingPayload.initialize();
		WaystoneNamePayload.initialize();
		WaystoneChanneling.initialize();
		AtlasSyncPayload.initialize();
		LOGGER.info("[wayfarer] common init");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
