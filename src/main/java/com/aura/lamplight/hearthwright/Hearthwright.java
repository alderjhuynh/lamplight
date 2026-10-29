package com.aura.lamplight.hearthwright;

import com.aura.lamplight.hearthwright.block.AmethystBlocks;
import com.aura.lamplight.hearthwright.block.ConcreteBlocks;
import com.aura.lamplight.hearthwright.block.StoneBlocks;
import com.aura.lamplight.hearthwright.block.TerracottaBlocks;
import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hearthwright implements ModInitializer {
	public static final String MOD_ID = "lamplight";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		TerracottaBlocks.register();
		StoneBlocks.register();
		ConcreteBlocks.register();
		AmethystBlocks.register();
		TerracottaBlocks.addCreativeTabEntries();
		StoneBlocks.addCreativeTabEntries();
		ConcreteBlocks.addCreativeTabEntries();
		AmethystBlocks.addCreativeTabEntries();
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
