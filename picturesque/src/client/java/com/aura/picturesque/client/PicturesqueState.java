package com.aura.picturesque.client;

import com.aura.picturesque.client.color.DayPhase;
import com.aura.picturesque.client.color.FogTint;
import com.aura.picturesque.client.color.LightMood;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;

/**
 * Per-frame cache of what Picturesque wants the world to feel like.
 *
 * <p>Updated once per client tick from time-of-day + weather (the Almanac
 * seam: when Almanac ships seasons and temperature, this is where its data
 * folds into {@link LightMood}). Mixins read from here so render code stays
 * branch-cheap: if the mod is disabled or the player is in a menu, they
 * early-out without touching vanilla values.
 */
public final class PicturesqueState {
    private static final PicturesqueState INSTANCE = new PicturesqueState();

    private volatile PicturesqueConfig config;
    private volatile LightMood mood = LightMood.forPhase(DayPhase.DAY, 0.0f);
    private volatile DayPhase phase = DayPhase.DAY;
    private volatile FogTint.BiomeKind biomeKind = FogTint.BiomeKind.DEFAULT;
    private volatile String biomeId = "minecraft:plains";
    private volatile float weatherDim;
    private volatile long dayTime;

    private PicturesqueState() {
    }

    public static PicturesqueState get() {
        return INSTANCE;
    }

    public void attach(PicturesqueConfig config) {
        this.config = config;
    }

    public PicturesqueConfig config() {
        return config;
    }

    public LightMood mood() {
        return mood;
    }

    public DayPhase phase() {
        return phase;
    }

    public FogTint.BiomeKind biomeKind() {
        return biomeKind;
    }

    public String biomeId() {
        return biomeId;
    }

    public boolean active() {
        return config != null && config.enabled;
    }

    /** Called from the client tick: cheap, allocation-free in the common path. */
    public void tick(Minecraft client) {
        if (config == null || !config.enabled) {
            return;
        }
        ClientLevel level = client.level;
        if (level == null || client.player == null) {
            return;
        }
        dayTime = level.getDefaultClockTime();
        phase = DayPhase.forTime(dayTime);

        float rain = 0.0f;
        try {
            rain = level.getRainLevel(1.0f);
        } catch (Exception ignored) {
            rain = 0.0f;
        }
        float thunder = 0.0f;
        try {
            thunder = level.getThunderLevel(1.0f);
        } catch (Exception ignored) {
            thunder = 0.0f;
        }
        weatherDim = Math.min(1.0f, Math.max(rain * 0.7f, thunder));

        Camera camera = client.gameRenderer.mainCamera();
        BlockPos pos = camera.blockPosition();
        try {
            String id = level.getBiome(pos).unwrapKey()
                    .map(key -> key.identifier().toString())
                    .orElse("minecraft:plains");
            biomeId = id;
            biomeKind = FogTint.classify(id);
        } catch (Exception ignored) {
            biomeKind = FogTint.BiomeKind.DEFAULT;
        }

        mood = LightMood.forPhase(phase, weatherDim);
    }

    public float gradeStrength() {
        PicturesqueConfig c = config;
        if (c == null || !c.enabled) {
            return 0.0f;
        }
        return c.gradeStrength;
    }

    public float fogStrength() {
        PicturesqueConfig c = config;
        if (c == null || !c.enabled) {
            return 0.0f;
        }
        return c.fogStrength;
    }
}
