package com.aura.lamplight.menagerie.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.item.ItemDisplayContext;

public class RaccoonHeldItemLayer extends RenderLayer<RaccoonRenderState, RaccoonModel> {
    public RaccoonHeldItemLayer(RaccoonRenderer renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack stack, SubmitNodeCollector collector, int light, RaccoonRenderState state, float yaw, float pitch) {
        if (state.heldItem.isEmpty() || state.raccoon == null) {
            return;
        }
        stack.pushPose();
        if (state.isBaby) {
            stack.scale(0.5F, 0.5F, 0.5F);
            stack.translate(0.0D, 1.5D, 0.0D);
        }
        stack.pushPose();
        boolean inHand = state.standProgress > 0.01F || state.washProgress > 0.01F || state.eatProgress > 0.01F;
        translateToHand(inHand, stack);
        if (inHand) {
            stack.translate(0.2F, 0.4F, 0.0F);
            stack.mulPose(Axis.XP.rotationDegrees(90.0F * state.washProgress * 0.2F));
        } else {
            stack.translate(0.0F, 1.5F, -9.0F);
        }
        stack.mulPose(Axis.YP.rotationDegrees(-2.5F));
        stack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        stack.scale(0.75F, 0.75F, 0.75F);
        ItemInHandRenderer itemRenderer = Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer();
        itemRenderer.renderItem(state.raccoon, state.heldItem, ItemDisplayContext.GROUND, stack, collector, light);
        stack.popPose();
        stack.popPose();
    }

    protected void translateToHand(boolean inHand, PoseStack stack) {
        if (inHand) {
            getParentModel().root().translateAndRotate(stack);
            getParentModel().body().translateAndRotate(stack);
            getParentModel().armRight().translateAndRotate(stack);
        } else {
            getParentModel().root().translateAndRotate(stack);
            getParentModel().body().translateAndRotate(stack);
            stack.translate(0.0F, 1.5F, -9.0F);
        }
    }
}
