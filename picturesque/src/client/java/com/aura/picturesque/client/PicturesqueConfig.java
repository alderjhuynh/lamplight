package com.aura.picturesque.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Player-facing toggles for Picturesque.
 *
 * <p>Everything defaults ON at gentle strength: the pack promise is "snug
 * first impression", and every effect is a steer of vanilla color rather
 * than a replacement, so disabling is always safe. Full path-traced bounce
 * and wide-penumbra shadows arrive later behind {@code raytracing}; v1 only
 * uses the cheap color/fog/light path.
 */
public final class PicturesqueConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public boolean enabled = true;
    /** Master opacity for the gouache grade (0 bypass .. 1 full). */
    public float gradeStrength = 0.65f;
    /** How much lighting kiss fog gets (mornings lavender, dusks peach). Biome colors lead. */
    public float fogStrength = 0.45f;
    /** Warm sunlight + amber lantern emphasis in the lightmap. */
    public boolean warmLight = true;
    /** Periwinkle/mauve shadows and plum/navy lifted blacks. */
    public boolean cozyShadows = true;
    /** Exaggerated bounce tint on ambient light. */
    public boolean bounceLight = true;
    /** Lit-up underwater fog: brighter, same hue — never repainted white. */
    public boolean jellyWater = true;
    /** Marshmallow sky + backlit-rim sunrise/sunset warming. */
    public boolean marshmallowSky = true;
    /** Golden motes drifting through leaves and windows (cheap particles). */
    public boolean lightShaftMotes = true;
    /**
     * Placeholder for the future full raytracing pass (soft shadows, true
     * bounce, blurred reflections, caustics). Parsed now so the option
     * exists in the file; ignored by v1 rendering.
     */
    public boolean raytracing = false;

    public static PicturesqueConfig load() {
        Path path = file();
        if (Files.isRegularFile(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                PicturesqueConfig loaded = GSON.fromJson(reader, PicturesqueConfig.class);
                if (loaded != null) {
                    loaded.clamp();
                    return loaded;
                }
            } catch (Exception e) {
                // Corrupt config: fall through to defaults and overwrite below.
            }
        }
        PicturesqueConfig fresh = new PicturesqueConfig();
        fresh.save();
        return fresh;
    }

    public void save() {
        clamp();
        try {
            Files.createDirectories(file().getParent());
            try (Writer writer = Files.newBufferedWriter(file())) {
                GSON.toJson(this, writer);
            }
        } catch (IOException ignored) {
            // Config is best-effort; rendering must never fail because of it.
        }
    }

    private void clamp() {
        gradeStrength = clamp01(gradeStrength);
        fogStrength = clamp01(fogStrength);
    }

    private static float clamp01(float v) {
        return v < 0.0f ? 0.0f : (v > 1.0f ? 1.0f : v);
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("picturesque.json");
    }
}
