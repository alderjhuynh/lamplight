package com.aura.menagerie.client;

import com.aura.menagerie.registry.MenagerieEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;

public class MenagerieClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModelLayerRegistry.registerModelLayer(CrowModelLayers.MODEL_LAYER, CrowModel::createBodyLayer);
		EntityRendererRegistry.register(MenagerieEntities.CROW, CrowRenderer::new);
		LivingEntityRenderLayerRegistrationCallback.EVENT.register((entityType, entityRenderer, helper, context) -> {
			if (entityRenderer instanceof AvatarRenderer<?> avatarRenderer) {
				helper.register(new CrowOnShoulderLayer(avatarRenderer, context.getModelSet()));
			}
		});
	}
}
