package com.aura.lamplight.wayfarer.client.waystone;

import com.aura.lamplight.wayfarer.Wayfarer;
import com.aura.lamplight.wayfarer.waystone.WaystoneBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import org.joml.Quaternionf;

public class WaystoneSigilRenderer implements BlockEntityRenderer<WaystoneBlockEntity, WaystoneSigilRenderer.SigilState> {
    private static final Identifier TEXTURE = Wayfarer.id("textures/entity/waystone_sigil.png");
    private static final float SIGIL_SIZE = 2F;
    private static final float SIGIL_HEIGHT = 0.5F;
    private static final long SPIN_PERIOD_MS = 45_000L;

    @Override
    public SigilState createRenderState() {
        return new SigilState();
    }

    @Override
    public int getViewDistance() {
        return 96;
    }

    @Override
    public void submit(SigilState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        pose.pushPose();
        pose.translate(0.5F, SIGIL_HEIGHT, 0.5F);
        pose.mulPose(camera.orientation);
        float spin = (float) ((System.currentTimeMillis() % SPIN_PERIOD_MS) * (Math.PI * 2.0F / SPIN_PERIOD_MS));
        pose.mulPose(new Quaternionf().rotationZ(spin));
        collector.submitCustomGeometry(pose, RenderTypes.eyes(TEXTURE), (leaf, vertex) -> {
            float half = SIGIL_SIZE / 2.0F;
            addQuadVertex(vertex, leaf, -half, -half, 0.0F, 0.0F, 0.0F);
            addQuadVertex(vertex, leaf,  half, -half, 0.0F, 1.0F, 0.0F);
            addQuadVertex(vertex, leaf,  half,  half, 0.0F, 1.0F, 1.0F);
            addQuadVertex(vertex, leaf, -half,  half, 0.0F, 0.0F, 1.0F);
        });
        pose.popPose();
    }

    private static void addQuadVertex(VertexConsumer vertex, PoseStack.Pose pose,
                                      float x, float y, float z, float u, float v) {
        vertex.addVertex(pose, x, y, z)
                .setUv(u, v)
                .setOverlay(0)
                .setLight(0xF000F0)
                .setNormal(pose, 0.0F, 0.0F, 1.0F)
                .setColor(0xFFFFFFFF);
    }

    public static class SigilState extends BlockEntityRenderState {
    }
}
