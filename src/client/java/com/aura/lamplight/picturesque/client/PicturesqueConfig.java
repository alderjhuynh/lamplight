package com.aura.lamplight.picturesque.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PicturesqueConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public boolean enabled = true;
    public float gradeStrength = 0.65f;
    public float fogStrength = 0.45f;
    public boolean warmLight = true;
    public boolean cozyShadows = true;
    public boolean bounceLight = true;
    public boolean jellyWater = true;
    public boolean marshmallowSky = true;
    public boolean lightShaftMotes = true;
    public boolean raytracing = false;
    public float raytraceStrength = 0.85f;
    public float vibrantBoost = 0.6f;
    public boolean raytraceBloom = true;
    public boolean raytraceBake = true;
    public boolean sunRefresh = true;

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
        }
    }

    private void clamp() {
        gradeStrength = clamp01(gradeStrength);
        fogStrength = clamp01(fogStrength);
        raytraceStrength = clamp01(raytraceStrength);
        vibrantBoost = clamp01(vibrantBoost);
    }

    private static float clamp01(float v) {
        return v < 0.0f ? 0.0f : (v > 1.0f ? 1.0f : v);
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("picturesque.json");
    }
}
