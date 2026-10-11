package com.aura.menagerie.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.item.ItemDisplayContext;

public class CrowHeldItemLayer extends RenderLayer<CrowRenderState, CrowModel> {
    public CrowHeldItemLayer(CrowRenderer renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack stack, SubmitNodeCollector collector, int light, CrowRenderState state, float yaw, float pitch) {
        if (state.heldItem.isEmpty() || state.crow == null) {
            return;
        }
        stack.pushPose();
        if (state.isBaby) {
            stack.scale(0.5F, 0.5F, 0.5F);
            stack.translate(0.0D, 1.5D, 0.0D);
        }
        stack.pushPose();
        getParentModel().translateToBeak(stack);
        stack.translate(0, -0.09F, -0.125F);
        stack.mulPose(Axis.YP.rotationDegrees(-2.5F));
        stack.mulPose(Axis.XP.rotationDegrees(-90F));
        stack.scale(0.75F, 0.75F, 0.75F);
        ItemInHandRenderer renderer = Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer();
        renderer.renderItem(state.crow, state.heldItem, ItemDisplayContext.GROUND, stack, collector, light);
        stack.popPose();
        stack.popPose();
    }
}
