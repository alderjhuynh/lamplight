package com.aura.lamplight.wayfarer.client.render;

import com.aura.lamplight.wayfarer.Wayfarer;
import com.aura.lamplight.wayfarer.client.WayfarerClient;
import com.aura.lamplight.wayfarer.client.minimap.MinimapChunkKey;
import com.aura.lamplight.wayfarer.client.minimap.MinimapProcessor;
import com.aura.lamplight.wayfarer.client.minimap.MinimapTextureCache;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.Mth;

public final class MinimapRenderer implements HudElement {
    private static final int MARGIN = 8;
    private static final int UNEXPLORED = MapPalette.UNEXPLORED;
    private static final String[] COMPASS_LABELS = {"N", "E", "S", "W"};
    private static final float[] COMPASS_X = {0.0f, 1.0f, 0.0f, -1.0f};
    private static final float[] COMPASS_Z = {-1.0f, 0.0f, 1.0f, 0.0f};

    private final MinimapProcessor processor = new MinimapProcessor();
    private MinimapTextureCache textures;
    private boolean firstFrame = true;
    private boolean lastCaveMode;

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, DeltaTracker deltaTracker) {
        try {
            var mc = Minecraft.getInstance();
            var config = WayfarerClient.CONFIG;
            if (config == null || !config.minimapEnabled) return;
            if (mc.player == null || mc.level == null) return;
            if (mc.gui.screen() != null) return;
            if (WayfarerClient.currentStorage() == null) return;
            if (WayfarerClient.minimapStore() == null) return;

            if (textures == null) {
                textures = new MinimapTextureCache(WayfarerClient.minimapStore());
                lastCaveMode = WayfarerClient.CAVE.isCave();
            }
            boolean caveMode = WayfarerClient.CAVE.isCave();
            if (caveMode != lastCaveMode) {
                textures.clear();
                lastCaveMode = caveMode;
            }

            int size = Math.max(64, Math.min(256, config.minimapSize));
            float partial = deltaTracker.getGameTimeDeltaPartialTick(false);
            double px = Mth.lerp(partial, mc.player.xo, mc.player.getX());
            double pz = Mth.lerp(partial, mc.player.zo, mc.player.getZ());
            float yaw = Mth.rotLerp(partial, mc.player.yRotO, mc.player.getYRot());

            double zoom = processor.updateZoom(Mth.clamp((float) config.minimapZoom, 0.25f, 8.0f));
            int renderDist = Math.max(2, Math.min(32, mc.options.getEffectiveRenderDistance()));
            double blocksAcross = MinimapProcessor.blocksAcross(renderDist);
            boolean rotated = true;
            double radiusBlocks = MinimapProcessor.radiusBlocks(blocksAcross, zoom, rotated);
            double ppb = (size / blocksAcross) * zoom; // pixels per block
            ppb = Math.max(0.125, Math.min(16.0, ppb));

            int x0 = MARGIN;
            int y0 = MARGIN;

            g.fill(x0 - 2, y0 - 2, x0 + size + 2, y0 + size + 2, MapPalette.FRAME);
            g.fill(x0, y0, x0 + size, y0 + size, UNEXPLORED);

            textures.beginFrame();

            int xFloored = Mth.floor(px);
            int zFloored = Mth.floor(pz);
            float fracX = (float) (px - xFloored);
            float fracZ = (float) (pz - zFloored);

            var range = MinimapProcessor.visibleRange(xFloored, zFloored, radiusBlocks);

            float cx = x0 + size / 2.0f;
            float cy = y0 + size / 2.0f;
            float rot = (float) Math.toRadians(180.0 - yaw);

            var pose = g.pose();
            g.enableScissor(x0, y0, x0 + size, y0 + size);
            pose.pushMatrix();
            try {
                pose.translate(cx, cy);
                pose.rotate(rot);
                pose.scale((float) ppb, (float) ppb);
                pose.translate(-fracX, -fracZ);

                for (int mx = range.minMx(); mx <= range.maxMx(); mx++) {
                    for (int mz = range.minMz(); mz <= range.maxMz(); mz++) {
                        MinimapChunkKey key = new MinimapChunkKey(mx, mz);
                        var texId = textures.getOrUpload(key);
                        int relX = key.minBlockX() - xFloored;
                        int relZ = key.minBlockZ() - zFloored;
                        if (texId == null) continue;
                        try {
                            g.blit(RenderPipelines.GUI_TEXTURED, texId,
                                    relX, relZ, 0.0f, 0.0f, 64, 64, 64, 64, 64, 64);
                        } catch (Throwable t) {
                            Wayfarer.LOGGER.warn("[wayfarer] minimap chunk blit failed", t);
                        }
                    }
                }
            } finally {
                pose.popMatrix();
                g.disableScissor();
            }
            try {
                com.aura.lamplight.wayfarer.client.waypoint.render.WaypointMinimapOverlay.renderMinimapLabels(
                        g, x0, y0, size, px, pz, yaw, zoom, radiusBlocks, ppb);
            } catch (Throwable ignored) {}

            try {
                MinimapChunkKey center = MinimapChunkKey.fromBlock(xFloored, zFloored);
                int radiusChunks = (int) Math.ceil(radiusBlocks / MinimapChunkKey.BLOCK_SIZE) + 1;
                textures.prune(center, radiusChunks);
                if (mc.level.getGameTime() % 100 == 0) {
                    WayfarerClient.minimapStore().pruneFar(center.mx(), center.mz(), radiusChunks + 2);
                }
            } catch (Throwable ignored) {}

            g.outline(x0 - 2, y0 - 2, size + 4, size + 4, MapPalette.OUTLINE);

            var font = mc.font;
            float compassInset = (float) Mth.clamp(config.minimapCompassInset, 0.0, 64.0);
            drawCompass(g, font, x0, y0, size, yaw, compassInset);
            if (config.showCoordinates) {
                String coords = Mth.floor(mc.player.getX()) + " "
                        + Mth.floor(mc.player.getY()) + " "
                        + Mth.floor(mc.player.getZ());
                int textX = x0 + (size - font.width(coords)) / 2;
                int textY = y0 + size + 6;
                g.text(font, coords, textX, textY, MapPalette.HUD_TEXT, true);
            }

            if (firstFrame) {
                firstFrame = false;
                processor.snapZoom(zoom);
            }
        } catch (Throwable t) {
            Wayfarer.LOGGER.warn("[wayfarer] minimap render failed", t);
        }
    }

    private static void drawCompass(GuiGraphicsExtractor g, Font font, int x0, int y0, int size,
                                    float yaw, float inset) {
        float cx = x0 + size / 2.0f;
        float cy = y0 + size / 2.0f;
        float radius = Math.max(1.0f, size / 2.0f - inset);
        float rot = (float) Math.toRadians(180.0 - yaw);
        float cos = (float) Math.cos(rot);
        float sin = (float) Math.sin(rot);
        float minX = x0 + 2.0f;
        float minY = y0 + 2.0f;
        float maxX = x0 + size - 2.0f;
        float maxY = y0 + size - 2.0f;
        var pose = g.pose();
        for (int i = 0; i < COMPASS_LABELS.length; i++) {
            float dx = COMPASS_X[i];
            float dz = COMPASS_Z[i];
            float lx = (dx * cos - dz * sin) * radius;
            float ly = (dx * sin + dz * cos) * radius;
            String label = COMPASS_LABELS[i];
            int tw = font.width(label);
            float tx = Mth.clamp(cx + lx - tw / 2.0f, minX, maxX - tw);
            float ty = Mth.clamp(cy + ly - font.lineHeight / 2.0f, minY, maxY - font.lineHeight);
            pose.pushMatrix();
            try {
                pose.translate(tx, ty);
                g.text(font, label, 0, 0, MapPalette.HUD_TEXT, true);
            } finally {
                pose.popMatrix();
            }
        }
    }

    public void close() {
        if (textures != null) {
            try { textures.clear(); } catch (Exception ignored) {}
            textures = null;
        }
        firstFrame = true;
    }
}
