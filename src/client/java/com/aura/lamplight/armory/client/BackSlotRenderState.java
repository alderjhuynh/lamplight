package com.aura.lamplight.armory.client;

import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.renderer.item.ItemStackRenderState;

// extra render-state payload for back-slot item
public final class BackSlotRenderState {
	public static final RenderStateDataKey<ItemStackRenderState> BACK_ITEM_KEY = RenderStateDataKey.create();
	public static final RenderStateDataKey<Boolean> HAND_MODEL_KEY = RenderStateDataKey.create();

	private BackSlotRenderState() {
	}
}
