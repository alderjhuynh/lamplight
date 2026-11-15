package com.aura.lamplight.picturesque.client.splash;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;

final class SplashGeometry {
    private SplashGeometry() {}

    static final float RIPPLE_LIFT = 0.02F;
    static final float RING_LIFT = 0.03F;

    private static final float TEXEL = 1.0F / 16.0F;

    static void walls(VertexConsumer vc, PoseStack.Pose pose, float scale, float outset, int color, int light) {
        float half = SplashTextures.BOX_W * TEXEL * scale * 0.5F;
        float height = SplashTextures.BOX_H * TEXEL * scale;
        float vTop = (float) SplashTextures.WALL_TOP_V / SplashTextures.ATLAS_H;
        float vBottom = 1.0F;
        int q = SplashTextures.BOX_W;

        wall(vc, pose, +half, -half, -half, -half, 0, -1, 0 * q, height, vTop, vBottom, outset, color, light); // north
        wall(vc, pose, -half, -half, -half, +half, -1, 0, 1 * q, height, vTop, vBottom, outset, color, light); // west
        wall(vc, pose, -half, +half, +half, +half, 0, +1, 2 * q, height, vTop, vBottom, outset, color, light); // south
        wall(vc, pose, +half, +half, +half, -half, +1, 0, 3 * q, height, vTop, vBottom, outset, color, light); // east
    }

    private static void wall(VertexConsumer vc, PoseStack.Pose pose,
                             float lx, float lz, float rx, float rz, float nx, float nz,
                             int uTexel, float height, float vTop, float vBottom,
                             float outset, int color, int light) {
        float u0 = (float) uTexel / SplashTextures.ATLAS_W;
        float u1 = (float) (uTexel + SplashTextures.BOX_W) / SplashTextures.ATLAS_W;
        float ox = nx * outset;
        float oz = nz * outset;
        vertex(vc, pose, lx + ox, 0.0F, lz + oz, u0, vBottom, color, light, nx, 0.0F, nz);
        vertex(vc, pose, rx + ox, 0.0F, rz + oz, u1, vBottom, color, light, nx, 0.0F, nz);
        vertex(vc, pose, rx + ox, height, rz + oz, u1, vTop, color, light, nx, 0.0F, nz);
        vertex(vc, pose, lx + ox, height, lz + oz, u0, vTop, color, light, nx, 0.0F, nz);
    }

    static void ring(VertexConsumer vc, PoseStack.Pose pose, float scale, int color, int light) {
        float half = SplashTextures.BOX_W * TEXEL * scale * 0.5F;
        float u0 = (float) SplashTextures.RING_U / SplashTextures.ATLAS_W;
        float u1 = (float) (SplashTextures.RING_U + SplashTextures.BOX_W) / SplashTextures.ATLAS_W;
        float v0 = 0.0F;
        float v1 = (float) SplashTextures.BOX_W / SplashTextures.ATLAS_H;
        flat(vc, pose, half, RING_LIFT, u0, v0, u1, v1, color, light);
    }

    static void ripple(VertexConsumer vc, PoseStack.Pose pose, float scale, int color, int light) {
        float half = 16 * TEXEL * scale * 0.5F;
        flat(vc, pose, half, RIPPLE_LIFT, 0.0F, 0.0F, 1.0F, 1.0F, color, light);
    }

    private static void flat(VertexConsumer vc, PoseStack.Pose pose, float half, float y,
                             float u0, float v0, float u1, float v1, int color, int light) {
        vertex(vc, pose, -half, y, -half, u0, v0, color, light, 0.0F, 1.0F, 0.0F);
        vertex(vc, pose, -half, y, +half, u0, v1, color, light, 0.0F, 1.0F, 0.0F);
        vertex(vc, pose, +half, y, +half, u1, v1, color, light, 0.0F, 1.0F, 0.0F);
        vertex(vc, pose, +half, y, -half, u1, v0, color, light, 0.0F, 1.0F, 0.0F);
    }

    private static void vertex(VertexConsumer vc, PoseStack.Pose pose, float x, float y, float z,
                               float u, float v, int color, int light, float nx, float ny, float nz) {
        vc.addVertex(pose, x, y, z)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }
}
