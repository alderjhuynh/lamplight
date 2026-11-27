package com.aura.lamplight.folio;

import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aura.lamplight.folio.component.ModDataComponents;
import com.aura.lamplight.folio.effect.FolioEffectSystem;
import com.aura.lamplight.folio.entity.ModEntityTypes;
import com.aura.lamplight.folio.item.ModItems;
import com.aura.lamplight.folio.menu.FolioMenus;
import com.aura.lamplight.folio.server.DragonbladeHandler;
import com.aura.lamplight.folio.server.FolioBossBars;
import com.aura.lamplight.folio.server.FolioManaHandler;
import com.aura.lamplight.folio.server.FolioCastHandler;
import com.aura.lamplight.folio.server.GravityWellHandler;
import com.aura.lamplight.folio.server.ShieldHandler;
import com.aura.lamplight.folio.sound.FolioSounds;
import com.aura.lamplight.folio.spell.Spells;
import com.aura.lamplight.folio.attribute.FolioAttributes;
import com.aura.lamplight.folio.particle.ModParticles;
import com.aura.lamplight.folio.temporal.TemporalFields;

public class Folio implements ModInitializer {
	public static final String MOD_ID = "lamplight";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("folio registered");
		ModDataComponents.initialize();
		FolioMenus.initialize();
		DragonbladeStatePayload.initialize();
		DragonbladeTeleportRequestPayload.initialize();
		CastSpellPayload.initialize();
		ReleaseSpellPayload.initialize();
		SelectSpellPayload.initialize();
		OpenSpellbookPayload.initialize();
		CastingStatePayload.initialize();
		SpellCooldownPayload.initialize();
		ModParticles.register();
		FolioAttributes.initialize();
		ModEntityTypes.initialize();
		FolioEffectSystem.initialize();
		FolioSounds.initialize();
		Spells.initialize();
		DragonbladeHandler.initialize();
		GravityWellHandler.initialize();
		ShieldHandler.initialize();
		FolioManaHandler.initialize();
		FolioCastHandler.initialize();
		FolioBossBars.initialize();
		TemporalFields.initialize();
		ModItems.initialize();
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
