package com.aura.lamplight.menagerie.client.wisp;

import com.aura.lamplight.menagerie.wisp.WispEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * A deliberately plain cube, with a pixel face and a few chunky hanging
 * blocks. The shader supplies the pale blue wash, the face, the back rune and
 * the pale edge, so there is no model or texture asset behind it.
 */
public class WispRenderer extends EntityRenderer<WispEntity, WispRenderState> {
    private static final float CUBE_CENTER_Y = 0.55F;
    private static final float BODY_HALF_X = 0.25F;
    private static final float BODY_HALF_Y = 0.25F;
    private static final float BODY_HALF_Z = 0.22F;

    public WispRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.15F;
        this.shadowStrength = 0.3F;
    }

    @Override
    public WispRenderState createRenderState() {
        return new WispRenderState();
    }

    @Override
    public void extractRenderState(WispEntity entity, WispRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.warmth = entity.getWarmth() / (float) WispEntity.MAX_WARMTH;
        state.flare = entity.hurtTime / 10.0F;
        state.tamed = entity.isTame();
        state.baby = entity.isBaby();
        state.sitting = entity.isWispSitting();
        state.yaw = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());

        Vec3 velocity = entity.getDeltaMovement();
        double horizontal = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
        state.pitch = (float) (-Mth.atan2(velocity.y, Math.max(horizontal, 0.05D)) * Mth.RAD_TO_DEG);
        state.pitch = Mth.clamp(state.pitch, -20.0F, 20.0F);
    }

    @Override
    protected int getBlockLightLevel(WispEntity entity, BlockPos pos) {
        return Mth.clamp(super.getBlockLightLevel(entity, pos) + 7, 0, 15);
    }

    @Override
    public void submit(WispRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.isInvisible) {
            super.submit(state, poseStack, collector, camera);
            return;
        }

        float warmth = state.tamed ? state.warmth : 0.0F;
        float flare = Mth.clamp(state.flare, 0.0F, 1.0F);

        float brightness = (state.tamed ? 0.95F + 0.05F * warmth : 1.0F) + 0.12F * flare;
        float red = Mth.lerp(warmth, 0.68F, 0.88F) * brightness + 0.16F * flare;
        float green = Mth.lerp(warmth, 0.84F, 0.90F) * brightness + 0.10F * flare;
        float blue = Mth.lerp(warmth, 0.96F, 0.94F) * brightness + 0.05F * flare;

        poseStack.pushPose();
        float scale = (state.baby ? 0.62F : 1.0F) * (state.sitting ? 0.95F : 1.0F);
        poseStack.scale(scale, scale, scale);
        float bob = (float) Math.sin(state.ageInTicks * 0.075F) * 0.015F;
        poseStack.translate(0.0F, CUBE_CENTER_Y + bob - (state.sitting ? 0.04F : 0.0F), 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(state.pitch * 0.5F));

        collector.submitCustomGeometry(poseStack, WispRenderTypes.WISP, (pose, buffer) ->
                submitWisp(pose, buffer, red, green, blue));

        poseStack.popPose();

        super.submit(state, poseStack, collector, camera);
    }

    private static void submitWisp(
            PoseStack.Pose pose, VertexConsumer buffer, float red, float green, float blue) {
        submitBody(pose, buffer, red, green, blue);

        // Short, square hanging blocks: a blocky nod to the reference ghost's
        // ragged hem and the baby happy ghast's stubby tentacles.
        submitHangingBlock(pose, buffer, -0.15F, 0.10F, 0.11F, 0.035F, 0.035F, red, green, blue);
        submitHangingBlock(pose, buffer, 0.00F, 0.19F, 0.14F, 0.040F, 0.030F, red, green, blue);
        submitHangingBlock(pose, buffer, 0.15F, 0.06F, 0.09F, 0.030F, 0.030F, red, green, blue);
        submitHangingBlock(pose, buffer, -0.06F, -0.16F, 0.12F, 0.035F, 0.035F, red, green, blue);

        submitDroplet(pose, buffer, 0.15F, -0.44F, 0.10F, 0.025F, red, green, blue);
        submitDroplet(pose, buffer, -0.17F, -0.49F, -0.03F, 0.020F, red, green, blue);
    }

    private static void submitBody(
            PoseStack.Pose pose, VertexConsumer buffer, float red, float green, float blue) {
        float hx = BODY_HALF_X;
        float hy = BODY_HALF_Y;
        float hz = BODY_HALF_Z;

        // Front (+Z) carries the face; back (-Z) carries the rune; top (+Y)
        // carries a small mark. The face ID lives in the integer part of UV0.x.
        boxFace(pose, buffer, 0.0F, 0.0F, hz, hx, 0.0F, 0.0F, 0.0F, hy, 0.0F,
                0.0F, 0.0F, 1.0F, 0, true, red, green, blue);
        boxFace(pose, buffer, 0.0F, 0.0F, -hz, -hx, 0.0F, 0.0F, 0.0F, hy, 0.0F,
                0.0F, 0.0F, -1.0F, 1, true, red, green, blue);
        boxFace(pose, buffer, hx, 0.0F, 0.0F, 0.0F, 0.0F, -hz, 0.0F, hy, 0.0F,
                1.0F, 0.0F, 0.0F, 2, true, red, green, blue);
        boxFace(pose, buffer, -hx, 0.0F, 0.0F, 0.0F, 0.0F, hz, 0.0F, hy, 0.0F,
                -1.0F, 0.0F, 0.0F, 3, true, red, green, blue);
        boxFace(pose, buffer, 0.0F, hy, 0.0F, hx, 0.0F, 0.0F, 0.0F, 0.0F, -hz,
                0.0F, 1.0F, 0.0F, 4, true, red, green, blue);
        boxFace(pose, buffer, 0.0F, -hy, 0.0F, hx, 0.0F, 0.0F, 0.0F, 0.0F, hz,
                0.0F, -1.0F, 0.0F, 5, true, red, green, blue);
    }

    private static void submitHangingBlock(
            PoseStack.Pose pose, VertexConsumer buffer,
            float centerX, float centerZ, float length, float halfX, float halfZ,
            float red, float green, float blue) {
        float topY = -BODY_HALF_Y + 0.01F;
        float centerY = topY - length * 0.5F;
        partBox(pose, buffer, centerX, centerY, centerZ, halfX, length * 0.5F, halfZ,
                6, red, green, blue);
    }

    private static void submitDroplet(
            PoseStack.Pose pose, VertexConsumer buffer,
            float centerX, float centerY, float centerZ, float halfSize,
            float red, float green, float blue) {
        partBox(pose, buffer, centerX, centerY, centerZ, halfSize, halfSize, halfSize,
                7, red, green, blue);
    }

    private static void partBox(
            PoseStack.Pose pose, VertexConsumer buffer,
            float cx, float cy, float cz, float hx, float hy, float hz,
            int faceId, float red, float green, float blue) {
        boxFace(pose, buffer, cx, cy, cz + hz, hx, 0.0F, 0.0F, 0.0F, hy, 0.0F,
                0.0F, 0.0F, 1.0F, faceId, false, red, green, blue);
        boxFace(pose, buffer, cx, cy, cz - hz, -hx, 0.0F, 0.0F, 0.0F, hy, 0.0F,
                0.0F, 0.0F, -1.0F, faceId, false, red, green, blue);
        boxFace(pose, buffer, cx + hx, cy, cz, 0.0F, 0.0F, -hz, 0.0F, hy, 0.0F,
                1.0F, 0.0F, 0.0F, faceId, false, red, green, blue);
        boxFace(pose, buffer, cx - hx, cy, cz, 0.0F, 0.0F, hz, 0.0F, hy, 0.0F,
                -1.0F, 0.0F, 0.0F, faceId, false, red, green, blue);
        boxFace(pose, buffer, cx, cy + hy, cz, hx, 0.0F, 0.0F, 0.0F, 0.0F, -hz,
                0.0F, 1.0F, 0.0F, faceId, false, red, green, blue);
        boxFace(pose, buffer, cx, cy - hy, cz, hx, 0.0F, 0.0F, 0.0F, 0.0F, hz,
                0.0F, -1.0F, 0.0F, faceId, false, red, green, blue);
    }

    private static void boxFace(
            PoseStack.Pose pose, VertexConsumer buffer,
            float cx, float cy, float cz,
            float ux, float uy, float uz,
            float vx, float vy, float vz,
            float nx, float ny, float nz,
            int faceId, boolean textured,
            float red, float green, float blue) {
        float u0 = textured ? faceId : faceId + 0.5F;
        float u1 = textured ? faceId + 0.999F : faceId + 0.5F;
        float v0 = textured ? 0.0F : 0.5F;
        float v1 = textured ? 1.0F : 0.5F;

        boxVertex(pose, buffer, cx - ux - vx, cy - uy - vy, cz - uz - vz,
                u0, v0, nx, ny, nz, red, green, blue);
        boxVertex(pose, buffer, cx + ux - vx, cy + uy - vy, cz + uz - vz,
                u1, v0, nx, ny, nz, red, green, blue);
        boxVertex(pose, buffer, cx + ux + vx, cy + uy + vy, cz + uz + vz,
                u1, v1, nx, ny, nz, red, green, blue);
        boxVertex(pose, buffer, cx - ux + vx, cy - uy + vy, cz - uz + vz,
                u0, v1, nx, ny, nz, red, green, blue);
    }

    private static void boxVertex(
            PoseStack.Pose pose, VertexConsumer buffer,
            float x, float y, float z, float u, float v,
            float nx, float ny, float nz,
            float red, float green, float blue) {
        buffer.addVertex(pose, x, y, z)
                .setColor(clampByte(red), clampByte(green), clampByte(blue), 255)
                .setUv(u, v)
                .setNormal(pose, nx, ny, nz);
    }

    private static int clampByte(float value) {
        return Mth.clamp((int) (value * 255.0F), 0, 255);
    }
}
