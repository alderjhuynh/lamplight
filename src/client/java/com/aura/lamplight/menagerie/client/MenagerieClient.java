package com.aura.lamplight.menagerie.client;

// Wisp disabled for now.
// import com.aura.lamplight.menagerie.client.wisp.WispRenderer;
// import com.aura.lamplight.menagerie.client.wisp.WispRenderTypes;
import com.aura.lamplight.menagerie.registry.MenagerieEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;

@SuppressWarnings("all")
public class MenagerieClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModelLayerRegistry.registerModelLayer(CrowModelLayers.MODEL_LAYER, CrowModel::createBodyLayer);
		EntityRendererRegistry.register(MenagerieEntities.CROW, CrowRenderer::new);
		// Wisp disabled for now.
		// WispRenderTypes.init();
		// EntityRendererRegistry.register(MenagerieEntities.WISP, WispRenderer::new);
		LivingEntityRenderLayerRegistrationCallback.EVENT.register((entityType, entityRenderer, helper, context) -> {
			if (entityRenderer instanceof AvatarRenderer<?> avatarRenderer) {
				helper.register(new CrowOnShoulderLayer(avatarRenderer, context.getModelSet()));
			}
		});
	}
}
