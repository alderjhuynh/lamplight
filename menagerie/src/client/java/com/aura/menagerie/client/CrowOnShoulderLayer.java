package com.aura.menagerie.client;

import com.aura.menagerie.Menagerie;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class CrowOnShoulderLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    private static final Identifier TEXTURE = Menagerie.id("textures/entity/crow.png");
    private final CrowModel model;

    public CrowOnShoulderLayer(RenderLayerParent<AvatarRenderState, PlayerModel> renderer, EntityModelSet modelSet) {
        super(renderer);
        this.model = new CrowModel(modelSet.bakeLayer(CrowModelLayers.MODEL_LAYER));
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords,
            AvatarRenderState state, float yRot, float xRot) {
        if (!(state instanceof CrowShoulderRenderState crowState)) {
            return;
        }
        if (crowState.menagerie$hasCrowOnLeftShoulder()) {
            submitCrow(poseStack, submitNodeCollector, lightCoords, state, yRot, xRot, true);
        }
        if (crowState.menagerie$hasCrowOnRightShoulder()) {
            submitCrow(poseStack, submitNodeCollector, lightCoords, state, yRot, xRot, false);
        }
    }

    private void submitCrow(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords,
            AvatarRenderState playerState, float yRot, float xRot, boolean isLeft) {
        poseStack.pushPose();
        poseStack.translate(isLeft ? 0.4F : -0.4F, playerState.isCrouching ? -1.3F : -1.5F, 0.0F);
        CrowRenderState crowState = new CrowRenderState();
        crowState.ageInTicks = playerState.ageInTicks;
        crowState.walkAnimationPos = 0.0F;
        crowState.walkAnimationSpeed = 0.0F;
        crowState.flyProgress = 0.0F;
        crowState.sitProgress = 1.0F;
        crowState.attackProgress = 0.0F;
        crowState.yRot = yRot;
        crowState.xRot = xRot;
        crowState.heldItem = ItemStack.EMPTY;
        submitNodeCollector.submitModel(this.model, crowState, poseStack, TEXTURE, lightCoords,
                OverlayTexture.NO_OVERLAY, playerState.outlineColor, null);
        poseStack.popPose();
    }
}
