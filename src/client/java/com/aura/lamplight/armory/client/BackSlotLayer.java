package com.aura.lamplight.armory.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

// renders back slot item against player's back, following body rot
public class BackSlotLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
	public BackSlotLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
		super(parent);
	}

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, AvatarRenderState state, float limbAngle, float limbDistance) {
		if (state.isInvisible) {
			return;
		}
		ItemStackRenderState itemState = ((FabricRenderState) (Object) state).getData(BackSlotRenderState.BACK_ITEM_KEY);
		if (itemState == null || itemState.isEmpty()) {
			return;
		}
		poseStack.pushPose();
		getParentModel().body.translateAndRotate(poseStack);
		// fixed path uses the tuned fixed defaults; hand-model items use the 
		// baked hand profile.
		boolean handModel = Boolean.TRUE.equals(((FabricRenderState) (Object) state).getData(BackSlotRenderState.HAND_MODEL_KEY));
		BackSlotTransform transform = handModel ? BackSlotTransform.hand() : BackSlotTransform.get();
		poseStack.translate(transform.offsetX, transform.offsetY, transform.offsetZ);
		poseStack.mulPose(Axis.YP.rotationDegrees(transform.rotY));
		poseStack.mulPose(Axis.XP.rotationDegrees(transform.rotX));
		poseStack.mulPose(Axis.ZP.rotationDegrees(transform.rotZ));
		poseStack.scale(transform.scale, transform.scale, transform.scale);
		itemState.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, state.outlineColor);
		poseStack.popPose();
	}
}
