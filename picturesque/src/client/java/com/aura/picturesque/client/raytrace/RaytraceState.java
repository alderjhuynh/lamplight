package com.aura.picturesque.client.raytrace;

import com.aura.picturesque.client.PicturesqueConfig;
import com.aura.picturesque.client.PicturesqueState;
import com.aura.picturesque.client.color.DayPhase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;

public final class RaytraceState {
    private static final RaytraceState INSTANCE = new RaytraceState();

    private final float[] sunDir = new float[]{0.0f, 1.0f, 0.0f};
    private volatile float daylight = 1.0f;
    private volatile float cameraSun = 1.0f;
    private volatile double timeSeconds;
    private volatile boolean skyWorld = true;
    private volatile long dayTime;
    private volatile DayPhase phase = DayPhase.DAY;
    private volatile boolean phaseChanged;
    private volatile int tickCounter;

    private RaytraceState() {
    }

    public static RaytraceState get() {
        return INSTANCE;
    }

    public boolean active() {
        PicturesqueConfig config = PicturesqueState.get().config();
        return config != null && config.enabled && config.raytracing;
    }

    public float raytraceStrength() {
        PicturesqueConfig config = PicturesqueState.get().config();
        if (config == null || !config.enabled || !config.raytracing) {
            return 0.0f;
        }
        return config.raytraceStrength;
    }

    public float vibrantBoost() {
        PicturesqueConfig config = PicturesqueState.get().config();
        if (config == null || !config.enabled || !config.raytracing) {
            return 0.0f;
        }
        return config.vibrantBoost;
    }

    public float[] sunDir() {
        return sunDir;
    }

    public float daylight() {
        return daylight;
    }

    public float cameraSun() {
        return cameraSun;
    }

    public double timeSeconds() {
        return timeSeconds;
    }

    public boolean skyWorld() {
        return skyWorld;
    }

    public long dayTime() {
        return dayTime;
    }

    public DayPhase phase() {
        return phase;
    }

    public boolean consumePhaseChange() {
        boolean changed = phaseChanged;
        phaseChanged = false;
        return changed;
    }

    public void tick(Minecraft client) {
        if (!active()) {
            return;
        }
        ClientLevel level = client.level;
        if (level == null || client.player == null) {
            return;
        }
        tickCounter++;
        dayTime = level.getDefaultClockTime();
        timeSeconds = level.getGameTime() / 20.0;

        try {
            skyWorld = level.dimensionType().hasSkyLight() && !level.dimensionType().hasCeiling();
        } catch (Exception e) {
            skyWorld = true;
        }

        DayPhase now = DayPhase.forTime(dayTime);
        if (now != phase) {
            phase = now;
            phaseChanged = true;
        }

        float[] dir = new float[3];
        SunPath.sunDirection(dayTime, dir);
        float day = skyWorld ? SunPath.daylightFactor(dayTime) : 0.0f;
        sunDir[0] = dir[0];
        sunDir[1] = dir[1];
        sunDir[2] = dir[2];
        daylight = day;

        if (tickCounter % 4 == 0) {
            try {
                BlockPos cam = client.gameRenderer.mainCamera().blockPosition();
                if (skyWorld && Math.max(day, (1.0f - day) * 0.5f) > 0.03f) {
                    boolean useMoon = (1.0f - day) * 0.5f > day;
                    float[] l = useMoon ? new float[]{-dir[0], -dir[1], -dir[2]} : dir;
                    cameraSun = VoxelTracer.traceSunVisibility(level, cam, l, 2);
                } else {
                    cameraSun = 0.0f;
                }
            } catch (Exception e) {
                cameraSun = daylight;
            }
        }
    }

    public static void refreshSections(Minecraft client) {
        if (client.level == null || client.levelRenderer == null) {
            return;
        }
        try {
            BlockPos cam = client.gameRenderer.mainCamera().blockPosition();
            int radius;
            try {
                radius = client.options.getEffectiveRenderDistance();
            } catch (Exception e) {
                radius = 8;
            }
            if (radius < 2) {
                radius = 2;
            } else if (radius > 16) {
                radius = 16;
            }
            int cx = net.minecraft.core.SectionPos.blockToSectionCoord(cam.getX());
            int cz = net.minecraft.core.SectionPos.blockToSectionCoord(cam.getZ());
            int y0 = client.level.getMinSectionY();
            int y1 = client.level.getMaxSectionY();
            client.level.setSectionRangeDirty(cx - radius, y0, cz - radius, cx + radius, y1, cz + radius);
        } catch (Exception e) {
        }
    }
}
