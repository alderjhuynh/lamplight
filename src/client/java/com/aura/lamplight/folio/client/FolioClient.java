package com.aura.lamplight.folio.client;

import com.aura.lamplight.folio.CastingStatePayload;
import com.aura.lamplight.folio.DragonbladeStatePayload;
import com.aura.lamplight.folio.SpellCooldownPayload;
import com.aura.lamplight.folio.entity.ModEntityTypes;
import com.aura.lamplight.folio.menu.FolioMenus;
import com.aura.lamplight.folio.particle.ModParticles;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

public class FolioClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRenderers.register(ModEntityTypes.FROST_BOLT, ThrownItemRenderer::new);
		ParticleProviderRegistry.getInstance().register(ModParticles.CHAIN_LINK, ChainLinkParticle.Provider::new);
		HudElementRegistry.attachElementAfter(VanillaHudElements.CROSSHAIR,
			com.aura.lamplight.folio.Folio.id("dragonblade_vignette"), new DragonbladeVignette());

		MenuScreens.register(FolioMenus.SPELLBOOK, SpellbookScreen::new);
		FolioKeys.initialize();

		ClientPlayNetworking.registerGlobalReceiver(DragonbladeStatePayload.TYPE, (payload, context) -> {
			context.client().execute(() -> DragonbladeVignette.setActive(payload.active()));
		});
		ClientPlayNetworking.registerGlobalReceiver(CastingStatePayload.TYPE, (payload, context) -> {
			context.client().execute(() -> SpellbookClientState.applyCasting(payload));
		});
		ClientPlayNetworking.registerGlobalReceiver(SpellCooldownPayload.TYPE, (payload, context) -> {
			context.client().execute(() -> SpellbookClientState.applyCooldown(payload));
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			SpellbookClientState.refresh(client);
			SpellbookClientState.tick();
			if (client.player == null) {
				return;
			}

			Screen screen = client.gui.screen();
			if (screen != null) {
				while (FolioKeys.WHEEL.consumeClick()) {
				}
				while (FolioKeys.CAST.consumeClick()) {
				}
				return;
			}
			if (!SpellbookClientState.hasSpellbook()) {
				return;
			}

			if (FolioKeys.WHEEL.consumeClick()) {
				if (FolioKeys.WHEEL.isDown()) {
					client.gui.setScreen(new SpellWheelScreen());
				}
			} else if (FolioKeys.CAST.consumeClick()) {
				SpellbookClientState.castSelected();
			}
		});
	}
}
