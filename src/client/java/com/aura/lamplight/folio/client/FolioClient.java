package com.aura.lamplight.folio.client;

import com.aura.lamplight.folio.entity.ModEntityTypes;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

public class FolioClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.

		// Frost Bolt renders as a thrown item (currently Items.ICE, see FrostBoltEntity)
		// using the same renderer vanilla uses for snowballs/eggs/ender pearls.
		EntityRenderers.register(ModEntityTypes.FROST_BOLT, ThrownItemRenderer::new);
	}
}