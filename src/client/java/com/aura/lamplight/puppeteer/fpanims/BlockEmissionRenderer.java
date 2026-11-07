package com.aura.lamplight.puppeteer.fpanims;

import com.mojang.blaze3d.vertex.PoseStack;
import com.aura.lamplight.client.mixin.puppeteer.fpanims.EntityRenderDispatcherAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockEmissionRenderer {
    private BlockEmissionRenderer() {
    }

    public static void renderSingleBlockEmission(BlockState blockState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int combinedLight) {
        if (blockState.getRenderShape() != RenderShape.INVISIBLE) {
            EntityRenderDispatcher entityDispatch = Minecraft.getInstance().getEntityRenderDispatcher();
            BlockModelResolver resolver = ((EntityRenderDispatcherAccessor) (Object) entityDispatch).fpanims$getBlockModelResolver();
            BlockModelRenderState renderState = new BlockModelRenderState();
            resolver.update(renderState, blockState, BlockDisplayContext.create());
            renderState.submit(poseStack, submitNodeCollector, combinedLight, OverlayTexture.NO_OVERLAY, 0);
        }
    }
}
