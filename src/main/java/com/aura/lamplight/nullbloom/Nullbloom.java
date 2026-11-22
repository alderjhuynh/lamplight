package com.aura.lamplight.nullbloom;

import com.aura.lamplight.Lamplight;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Nullbloom {
	public static final Logger LOGGER = LoggerFactory.getLogger(Lamplight.MOD_ID + "/nullbloom");

	public static Identifier id(String path) {
		return Lamplight.id(path);
	}

	public void onInitialize() {
		NullbloomBlocks.register();
		NullbloomChunkGenerator.register();
		LOGGER.info("Registered nullbloom");
	}
}
