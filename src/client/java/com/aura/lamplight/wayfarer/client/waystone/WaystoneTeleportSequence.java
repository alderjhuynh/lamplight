package com.aura.lamplight.wayfarer.client.waystone;

import com.aura.lamplight.wayfarer.Wayfarer;
import com.aura.lamplight.wayfarer.waystone.ModParticles;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Quaternionf;

@SuppressWarnings ("all")
public final class WaystoneTeleportSequence implements HudElement {
    private static final Identifier VIGNETTE = Wayfarer.id("textures/gui/teleport_vignette.png");
    private static final Identifier SIGIL = Wayfarer.id("textures/entity/waystone_sigil.png");

    private static final float VIGNETTE_RAMP_TICKS = 40.0F;
    private static final float FLASH_START = 40.0F;
    private static final float FLASH_FULL = 57.0F;
    private static final float FLASH_HOLD = 80.0F;
    private static final float FLASH_END = 100.0F;
    private static final float ABORT_FADE = 8.0F;

    private static final float SIGIL_FADE_TICKS = 8.0F;
    private static final long SIGIL_PERIOD_MS = 30_000L;
    private static final float SIGIL_SIZE = 3.0F;
    private static final float SIGIL_LIFT = 0.03F;
    private static final int BURST_TICK_1 = 50;
    private static final int BURST_TICK_2 = 55;

    public enum Phase { NONE, CHANNEL, ABORT }

    private Phase phase = Phase.NONE;
    private long startMs;
    private int totalTicks = 60;

    private long abortMs;
    private float abortVignette;
    private float abortFlash;

    private int lastBurstTick = 0;

    private boolean warmed;

    public void begin(int totalTicks) {
        this.phase = Phase.CHANNEL;
        this.startMs = System.currentTimeMillis();
        this.totalTicks = totalTicks;
        this.lastBurstTick = 0;
    }

    public void abort() {
        if (this.phase != Phase.CHANNEL) {
            return;
        }
        long now = System.currentTimeMillis();
        float t = elapsedTicks(now);
        this.abortMs = now;
        this.abortVignette = vignetteAlpha(t);
        this.abortFlash = flashAlpha(t);
        this.phase = Phase.ABORT;
    }

    public void succeed() {
    }

    public void warm(Minecraft mc) {
        if (this.warmed || mc.player == null || mc.level == null) {
            return;
        }
        GpuDevice device = RenderSystem.tryGetDevice();
        if (device == null) {
            return;
        }
        this.warmed = true;
        try {
            device.precompilePipeline(RenderPipelines.GUI_TEXTURED);
            device.precompilePipeline(RenderPipelines.EYES);
            var vignette = mc.getTextureManager().getTexture(VIGNETTE);
            var sigil = mc.getTextureManager().getTexture(SIGIL);
            vignette.getTextureView();
            vignette.getSampler();
            sigil.getTextureView();
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, DeltaTracker deltaTracker) {
        if (this.phase == Phase.NONE) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            this.phase = Phase.NONE;
            return;
        }

        float vignette;
        float flash;
        if (this.phase == Phase.CHANNEL) {
            float t = elapsedTicks(System.currentTimeMillis());
            if (t >= FLASH_END) {
                this.phase = Phase.NONE;
                return;
            }
            vignette = vignetteAlpha(t);
            flash = flashAlpha(t);
            stageBursts(mc, t);
        } else {
            float t = (System.currentTimeMillis() - this.abortMs) / 50.0F;
            float fade = Math.max(0.0F, 1.0F - t / ABORT_FADE);
            vignette = this.abortVignette * fade;
            flash = this.abortFlash * fade;
            if (t >= ABORT_FADE) {
                this.phase = Phase.NONE;
            }
        }

        if (vignette > 0.008F) {
            g.blit(RenderPipelines.GUI_TEXTURED, VIGNETTE,
                    0, 0, 0.0F, 0.0F,
                    g.guiWidth(), g.guiHeight(), g.guiWidth(), g.guiHeight(),
                    ARGB.colorFromFloat(vignette, 1.0F, 1.0F, 1.0F));
        }
        if (flash > 0.008F) {
            g.fill(0, 0, g.guiWidth(), g.guiHeight(), ARGB.colorFromFloat(flash, 1.0F, 1.0F, 1.0F));
        }
    }

    private float elapsedTicks(long now) {
        return (now - this.startMs) / 50.0F;
    }

    private static float vignetteAlpha(float t) {
        float v = Mth.clamp(t / VIGNETTE_RAMP_TICKS, 0.0F, 1.0F);
        float ramp = v * v;
        if (t <= FLASH_HOLD) {
            return ramp;
        }
        float fade = Mth.clamp(1.0F - (t - FLASH_HOLD) / (FLASH_END - FLASH_HOLD), 0.0F, 1.0F);
        return ramp * fade;
    }

    private static float flashAlpha(float t) {
        float a;
        if (t <= FLASH_START) {
            a = 0.0F;
        } else if (t < FLASH_FULL) {
            a = Mth.clamp((t - FLASH_START) / (FLASH_FULL - FLASH_START), 0.0F, 1.0F);
        } else if (t <= FLASH_HOLD) {
            a = 1.0F;
        } else {
            a = Mth.clamp(1.0F - (t - FLASH_HOLD) / (FLASH_END - FLASH_HOLD), 0.0F, 1.0F);
        }
        return a;
    }

    private void stageBursts(Minecraft mc, float t) {
        int upto = (int) t;
        while (this.lastBurstTick < upto) {
            this.lastBurstTick++;
            spawnWave(mc, this.lastBurstTick);
        }
    }

    private static void spawnWave(Minecraft mc, int tick) {
        if (tick != BURST_TICK_1 && tick != BURST_TICK_2) {
            return;
        }
        int count = tick == BURST_TICK_1 ? 20 : 34;
        double ox = mc.player.getX();
        double oy = mc.player.getY() + 0.5;
        double oz = mc.player.getZ();
        RandomSource random = RandomSource.create();
        for (int i = 0; i < count; i++) {
            mc.level.addParticle(ModParticles.WAYSTONE_TELEPORT,
                    ox + (random.nextDouble() - 0.5) * 1.6,
                    oy + random.nextDouble() * 1.6,
                    oz + (random.nextDouble() - 0.5) * 1.6,
                    (random.nextDouble() - 0.5) * 0.12,
                    0.03 + random.nextDouble() * 0.15,
                    (random.nextDouble() - 0.5) * 0.12);
        }
    }

    public void collect(LevelRenderContext ctx) {
        if (this.phase != Phase.CHANNEL) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }
        float t = elapsedTicks(System.currentTimeMillis());
        if (t >= FLASH_FULL) {
            return;
        }
        float alpha = Mth.clamp(t / SIGIL_FADE_TICKS, 0.0F, 1.0F);
        if (alpha <= 0.0F) {
            return;
        }
        PoseStack pose = ctx.poseStack();
        SubmitNodeCollector collector = ctx.submitNodeCollector();
        if (pose == null || collector == null) {
            return;
        }
        CameraRenderState cam = ctx.levelState().cameraRenderState;
        if (cam == null || !cam.initialized || cam.pos == null) {
            return;
        }

        float px = (float) mc.player.getX();
        float py = (float) mc.player.getY();
        float pz = (float) mc.player.getZ();
        float spin = (float) ((System.currentTimeMillis() % SIGIL_PERIOD_MS)
                * (Math.PI * 2.0F / SIGIL_PERIOD_MS));
        int color = ARGB.colorFromFloat(alpha, 1.0F, 1.0F, 1.0F);
        submitFlatSigil(ctx, px, py, pz, spin, color);
    }

    private static void submitFlatSigil(LevelRenderContext ctx, float px, float py, float pz, float spin, int color) {
        PoseStack pose = ctx.poseStack();
        SubmitNodeCollector collector = ctx.submitNodeCollector();
        if (pose == null || collector == null) {
            return;
        }
        CameraRenderState cam = ctx.levelState().cameraRenderState;
        if (cam == null || !cam.initialized || cam.pos == null) {
            return;
        }

        pose.pushPose();
        try {
            pose.translate(px - (float) cam.pos.x, py - (float) cam.pos.y + SIGIL_LIFT, pz - (float) cam.pos.z);
            pose.mulPose(new Quaternionf().rotationY(-spin));
            float half = SIGIL_SIZE / 2.0F;
            collector.submitCustomGeometry(pose, RenderTypes.eyes(SIGIL), (leaf, vertex) -> {
                groundQuadVertex(vertex, leaf, -half, -half, 0.0F, 0.0F, color);
                groundQuadVertex(vertex, leaf,  half, -half, 1.0F, 0.0F, color);
                groundQuadVertex(vertex, leaf,  half,  half, 1.0F, 1.0F, color);
                groundQuadVertex(vertex, leaf, -half,  half, 0.0F, 1.0F, color);
                groundQuadVertex(vertex, leaf, -half, -half, 0.0F, 0.0F, color);
                groundQuadVertex(vertex, leaf, -half,  half, 0.0F, 1.0F, color);
                groundQuadVertex(vertex, leaf,  half,  half, 1.0F, 1.0F, color);
                groundQuadVertex(vertex, leaf,  half, -half, 1.0F, 0.0F, color);
            });
        } finally {
            pose.popPose();
        }
    }

    private static void groundQuadVertex(VertexConsumer vertex, PoseStack.Pose pose,
                                         float x, float z, float u, float v, int color) {
        vertex.addVertex(pose, x, 0.0F, z)
                .setUv(u, v)
                .setOverlay(0)
                .setLight(0xF000F0)
                .setNormal(pose, 0.0F, 1.0F, 0.0F)
                .setColor(color);
    }
}
