package com.aura.lamplight;

import com.aura.lamplight.vantage.Vantage;
import com.aura.lamplight.cairn.Cairn;
import com.aura.lamplight.almanac.Almanac;
import com.aura.lamplight.hearthwright.Hearthwright;
import com.aura.lamplight.ambrosia.Ambrosia;
import com.aura.lamplight.panacea.Panacea;
import com.aura.lamplight.tinkerer.Tinkerer;
import com.aura.lamplight.menagerie.Menagerie;
import com.aura.lamplight.gloaming.Gloaming;
import com.aura.lamplight.arcanum.Arcanum;
import com.aura.lamplight.armory.Armory;
import com.aura.lamplight.folio.Folio;
import com.aura.lamplight.wayfarer.Wayfarer;
import com.aura.lamplight.puppeteer.Puppeteer;
import com.aura.lamplight.picturesque.Picturesque;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Lamplight implements ModInitializer {
	public static final String MOD_ID = "lamplight";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		new Vantage().onInitialize();
		new Cairn().onInitialize();
		new Almanac().onInitialize();
		new Hearthwright().onInitialize();
		new Ambrosia().onInitialize();
		new Panacea().onInitialize();
		new Tinkerer().onInitialize();
		new Menagerie().onInitialize();
		new Gloaming().onInitialize();
		new Arcanum().onInitialize();
		new Armory().onInitialize();
		new Folio().onInitialize();
		new Wayfarer().onInitialize();
		new Puppeteer().onInitialize();
		new Picturesque().onInitialize();

		LOGGER.info("Lamplight initialized");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
