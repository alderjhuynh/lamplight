package com.aura.lamplight.armory.client;

import com.aura.lamplight.armory.BackSlotAttachments;
import com.aura.lamplight.armory.DrawSheathPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;

public class ArmoryClient implements ClientModInitializer {
	// master switch for transform editor. off now that tuning is baked in
	private static final boolean ENABLE_TRANSFORM_EDITOR = false;

	@Override
	public void onInitializeClient() {
		BackSlotTransform.load();

		KeyMapping.Category category = KeyMapping.Category.register(net.minecraft.resources.Identifier.fromNamespaceAndPath("lamplight", "lamplight"));

		if (ENABLE_TRANSFORM_EDITOR) {
			KeyMapping editTransformKey = KeyMappingHelper.registerKeyMapping(
					new KeyMapping("key.lamplight.backslot_edit", InputConstants.Type.KEYSYM, InputConstants.KEY_B, category));

			ClientTickEvents.END_CLIENT_TICK.register(client -> {
				if (editTransformKey.consumeClick() && client.gui.screen() == null && client.player != null) {
					client.gui.setScreen(new BackSlotTransformScreen());
				}
			});
		}

		KeyMapping drawSheathKey = KeyMappingHelper.registerKeyMapping(
				new KeyMapping("key.lamplight.draw_sheath", InputConstants.Type.KEYSYM, InputConstants.KEY_G, category));

		ClientTickEvents.END_CLIENT_TICK.register(new SelectionDisarmTracker(drawSheathKey));

		LivingEntityRenderLayerRegistrationCallback.EVENT.register((entityType, renderer, helper, context) -> {
			if (renderer instanceof AvatarRenderer<?> avatarRenderer) {
				helper.register(new BackSlotLayer(avatarRenderer));
			}
		});
	}

	// g toggles wielding flag, other hotbar selection changes sheathes. 
	private static final class SelectionDisarmTracker implements ClientTickEvents.EndTick {
		private final KeyMapping drawSheathKey;
		private int lastSelected = -1;

		private SelectionDisarmTracker(KeyMapping drawSheathKey) {
			this.drawSheathKey = drawSheathKey;
		}

		@Override
		public void onEndTick(net.minecraft.client.Minecraft client) {
			if (client.player == null) {
				lastSelected = -1;
				return;
			}
			if (!ClientPlayNetworking.canSend(DrawSheathPayload.TYPE)) {
				return;
			}
			int selected = client.player.getInventory().getSelectedSlot();
			boolean holding = BackSlotAttachments.isHolding(client.player);
			if (holding && lastSelected != -1 && selected != lastSelected) {
				BackSlotAttachments.setHolding(client.player, false);
				ClientPlayNetworking.send(new DrawSheathPayload(false));
				holding = false;
			}
			lastSelected = selected;
			if (!drawSheathKey.consumeClick() || client.gui.screen() != null) {
				return;
			}
			// skip round trip when there is nothing to draw and nothing held.
			if (!holding && BackSlotAttachments.getBackStack(client.player).isEmpty()) {
				return;
			}
			BackSlotAttachments.setHolding(client.player, !holding);
			ClientPlayNetworking.send(new DrawSheathPayload(!holding));
		}
	}
}
