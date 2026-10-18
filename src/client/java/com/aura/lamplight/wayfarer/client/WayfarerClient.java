package com.aura.lamplight.wayfarer.client;

import com.aura.lamplight.wayfarer.Wayfarer;
import com.aura.lamplight.wayfarer.client.config.WayfarerConfig;
import com.aura.lamplight.wayfarer.client.gui.WorldMapScreen;
import com.aura.lamplight.wayfarer.client.input.MapKeybindings;
import com.aura.lamplight.wayfarer.client.minimap.MinimapDataStore;
import com.aura.lamplight.wayfarer.client.render.MinimapRenderer;
import com.aura.lamplight.wayfarer.client.storage.RegionFileStorage;
import com.aura.lamplight.wayfarer.client.waypoint.WaypointManager;
import com.aura.lamplight.wayfarer.client.waypoint.gui.WaypointEditScreen;
import com.aura.lamplight.wayfarer.client.waypoint.gui.WaypointsScreen;
import com.aura.lamplight.wayfarer.client.waypoint.render.WaypointWorldRenderer;
import com.aura.lamplight.wayfarer.client.world.CaveModeTracker;
import com.aura.lamplight.wayfarer.client.world.DimensionContext;
import com.aura.lamplight.wayfarer.client.world.MapUpdateQueue;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;

public class WayfarerClient implements ClientModInitializer {
    public static WayfarerConfig CONFIG;
    public static final MapUpdateQueue UPDATE_QUEUE = new MapUpdateQueue();
    public static final CaveModeTracker CAVE = new CaveModeTracker();
    private static RegionFileStorage currentStorage;
    private static RegionFileStorage currentCaveStorage;
    private static final MinimapDataStore MINIMAP_STORE = new MinimapDataStore();
    private static final MinimapRenderer MINIMAP_RENDERER = new MinimapRenderer();
    private int sampleCursor = 0;

    @Override
    public void onInitializeClient() {
        CONFIG = WayfarerConfig.load();
        for (var kb : MapKeybindings.all()) {
            KeyMappingHelper.registerKeyMapping(kb);
        }
        HudElementRegistry.attachElementAfter(VanillaHudElements.CROSSHAIR, Wayfarer.id("minimap"),
                MINIMAP_RENDERER);
        WaypointWorldRenderer.register();
        UPDATE_QUEUE.setMinimapStore(MINIMAP_STORE);
        ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
        Wayfarer.LOGGER.info("[wayfarer] client init");
    }

    private void onEndTick(Minecraft mc) {
        if (mc.player == null || mc.level == null) {
            if (currentStorage != null) {
                UPDATE_QUEUE.flushAsyncForced();
                WaypointManager.get().saveNow();
                WaypointManager.get().clearContext();
                currentStorage = null;
                currentCaveStorage = null;
                CAVE.reset();
                MINIMAP_STORE.clear();
                MINIMAP_RENDERER.close();
            }
            return;
        }
        if (currentStorage == null || !isStorageCurrent(mc)) {
            currentStorage = DimensionContext.storageFor(mc.level);
            currentCaveStorage = DimensionContext.caveStorageFor(mc.level);
            UPDATE_QUEUE.setStorage(currentStorage);
            UPDATE_QUEUE.setCaveStorage(currentCaveStorage);
            UPDATE_QUEUE.setMinimapStore(MINIMAP_STORE);
            MINIMAP_STORE.clear();
            MINIMAP_RENDERER.close();
            CAVE.reset();
            WaypointManager.get().setContext(currentStorage.dimRoot(), DimensionContext.worldId());
        }
        updateCaveMode(mc);
        while (MapKeybindings.OPEN_MAP.consumeClick()) {
            openMap(mc);
        }
        while (MapKeybindings.OPEN_WAYPOINTS.consumeClick()) {
            mc.setScreenAndShow(new WaypointsScreen(mc.gui.screen()));
        }
        while (MapKeybindings.ADD_WAYPOINT.consumeClick()) {
            int x = (int) Math.floor(mc.player.getX());
            int y = (int) Math.floor(mc.player.getY());
            int z = (int) Math.floor(mc.player.getZ());
            mc.setScreenAndShow(new WaypointEditScreen(mc.gui.screen(), null, x, y, z));
        }
        if (CONFIG.waypointDeathpoints) {
            WaypointManager.get().tickDeathTracking(mc);
        }
        if (mc.level != null && mc.level.getGameTime() % 4 == 0) {
            sampleNearbyChunksThrottled(mc, 8);
        }
        if (mc.level != null && mc.player != null && currentStorage != null) {
            int pcx = mc.player.chunkPosition().x();
            int pcz = mc.player.chunkPosition().z();
            int radius = mc.options.getEffectiveRenderDistance();
            radius = Math.max(4, Math.min(radius, 16));
            if (CONFIG.mapWritingDistance >= 0) radius = Math.min(radius, CONFIG.mapWritingDistance);
            UPDATE_QUEUE.drainWithBudget(mc.level, pcx, pcz, radius);
        }
    }

    private boolean isStorageCurrent(Minecraft mc) {
        if (mc.level == null || currentStorage == null) return false;
        var expected = DimensionContext.storageFor(mc.level);
        if (!expected.dimRoot().equals(currentStorage.dimRoot())) return false;
        if (currentCaveStorage == null) return false;
        var expectedCave = DimensionContext.caveStorageFor(mc.level);
        return expectedCave.dimRoot().equals(currentCaveStorage.dimRoot());
    }

    private void updateCaveMode(Minecraft mc) {
        if (mc.level == null || mc.player == null) return;
        try {
            CAVE.tick(mc.level, mc.player.getX(), mc.player.getY(), mc.player.getZ(), CONFIG);
        } catch (Exception e) {
            Wayfarer.LOGGER.warn("[wayfarer] cave detection failed", e);
            CAVE.reset();
        }
        int depth = CONFIG != null ? Math.max(1, Math.min(64, CONFIG.caveModeDepth)) : 30;
        UPDATE_QUEUE.setCaveState(new MapUpdateQueue.CaveState(CAVE.isCave(), CAVE.current(), depth));
        MINIMAP_STORE.setCaveActive(CAVE.isCave());
    }

    private void sampleNearbyChunksThrottled(Minecraft mc, int budget) {
        if (mc.level == null || mc.player == null) return;
        int pcx = mc.player.chunkPosition().x();
        int pcz = mc.player.chunkPosition().z();
        int radius = mc.options.getEffectiveRenderDistance();
        radius = Math.max(4, Math.min(radius, 16));
        if (CONFIG.mapWritingDistance >= 0) radius = Math.min(radius, CONFIG.mapWritingDistance);
        int diameter = radius * 2 + 1;
        int total = diameter * diameter;
        for (int i = 0; i < budget; i++) {
            int idx = (sampleCursor + i) % total;
            int dx = (idx % diameter) - radius;
            int dz = (idx / diameter) - radius;
            int cx = pcx + dx;
            int cz = pcz + dz;
            var chunk = mc.level.getChunkSource().getChunk(cx, cz, false);
            if (chunk instanceof net.minecraft.world.level.chunk.LevelChunk lc) {
                UPDATE_QUEUE.enqueue(lc);
            }
        }
        sampleCursor = (sampleCursor + budget) % total;
    }

    private void openMap(Minecraft mc) {
        if (currentStorage == null && mc.level != null) {
            currentStorage = DimensionContext.storageFor(mc.level);
            UPDATE_QUEUE.setStorage(currentStorage);
        }
        if (currentCaveStorage == null && mc.level != null) {
            currentCaveStorage = DimensionContext.caveStorageFor(mc.level);
            UPDATE_QUEUE.setCaveStorage(currentCaveStorage);
        }
        if (currentStorage == null) {
            Wayfarer.LOGGER.warn("[wayfarer] no storage available to open map");
            return;
        }
        UPDATE_QUEUE.flushAsync();
        double x = mc.player != null ? mc.player.getX() : 0;
        double z = mc.player != null ? mc.player.getZ() : 0;
        mc.setScreenAndShow(new WorldMapScreen(currentStorage, currentCaveStorage, CONFIG, x, z));
    }

    public static RegionFileStorage currentStorage() { return currentStorage; }
    public static RegionFileStorage currentCaveStorage() { return currentCaveStorage; }
    public static MinimapDataStore minimapStore() { return MINIMAP_STORE; }
}
