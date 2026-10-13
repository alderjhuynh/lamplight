package com.aura.picturesque.client.color;

public final class FogTint {
    private FogTint() {
    }

    private static final float HUE_KISS = 0.35f;

    public enum BiomeKind {
        FOREST,
        PLAINS,
        DESERT,
        SNOWY,
        SWAMP,
        OCEAN,
        MOUNTAIN,
        NETHER,
        END,
        DEFAULT
    }

    public static BiomeKind classify(String biomeId) {
        if (biomeId == null) {
            return BiomeKind.DEFAULT;
        }
        String id = biomeId.toLowerCase();
        if (id.contains("nether") || id.contains("basalt") || id.contains("soul_sand") || id.contains("crimson") || id.contains("warped")) {
            return BiomeKind.NETHER;
        }
        if (id.contains("end_") || id.equals("minecraft:the_end") || id.contains("the_end")) {
            return BiomeKind.END;
        }
        if (id.contains("forest") || id.contains("taiga") || id.contains("jungle") || id.contains("cherry")
                || id.contains("birch") || id.contains("dark_oak") || id.contains("wooded") || id.contains("grove")) {
            return BiomeKind.FOREST;
        }
        if (id.contains("snow") || id.contains("frozen") || id.contains("ice")) {
            return BiomeKind.SNOWY;
        }
        if (id.contains("desert") || id.contains("badlands") || id.contains("savanna")) {
            return BiomeKind.DESERT;
        }
        if (id.contains("swamp") || id.contains("mangrove") || id.contains("bog")) {
            return BiomeKind.SWAMP;
        }
        if (id.contains("ocean") || id.contains("river") || id.contains("beach")) {
            return BiomeKind.OCEAN;
        }
        if (id.contains("mountain") || id.contains("peak") || id.contains("hill") || id.contains("cave")) {
            return BiomeKind.MOUNTAIN;
        }
        if (id.contains("plains") || id.contains("meadow") || id.contains("sunflower")) {
            return BiomeKind.PLAINS;
        }
        return BiomeKind.DEFAULT;
    }

    public static int targetFor(LightMood mood, BiomeKind kind) {
        int base = mood.fogTint();
        return switch (kind) {
            case FOREST -> PicturesquePalette.lerp(base, PicturesquePalette.FOG_MINT_FOREST, 0.45f);
            case SNOWY -> PicturesquePalette.lerp(base, PicturesquePalette.FOG_LAVENDER_MORNING, 0.35f);
            case DESERT -> PicturesquePalette.lerp(base, PicturesquePalette.FOG_HONEY_NOON, 0.35f);
            case SWAMP -> PicturesquePalette.lerp(base, PicturesquePalette.FOG_MINT_FOREST, 0.30f);
            case OCEAN -> PicturesquePalette.lerp(base, PicturesquePalette.WATER_LILAC, 0.25f);
            case NETHER, END -> base;
            default -> base;
        };
    }

    public static int blend(int vanillaFog, LightMood mood, BiomeKind kind, float fogStrength, float gradeStrength) {
        int target = targetFor(mood, kind);
        int mixed = PicturesquePalette.lerp(vanillaFog, target, clamp01(fogStrength) * HUE_KISS);
        return GouacheGrade.gradePacked(mixed, gradeStrength);
    }

    public static int liftWater(int vanillaWaterFog, float strength) {
        float s = clamp01(strength);
        if (s <= 0.001f) {
            return vanillaWaterFog;
        }
        float r = PicturesquePalette.red(vanillaWaterFog);
        float g = PicturesquePalette.green(vanillaWaterFog);
        float b = PicturesquePalette.blue(vanillaWaterFog);
        float luma = 0.2126f * r + 0.7152f * g + 0.0722f * b;

        float boost = 1.0f + (1.0f - smoothstep(0.0f, 0.7f, luma)) * 0.9f * s;
        r *= boost;
        g *= boost;
        b *= boost;

        float kiss = 0.12f * s;
        r = lerp(r, PicturesquePalette.red(PicturesquePalette.WATER_AQUA), kiss);
        g = lerp(g, PicturesquePalette.green(PicturesquePalette.WATER_AQUA), kiss);
        b = lerp(b, PicturesquePalette.blue(PicturesquePalette.WATER_AQUA), kiss);

        return PicturesquePalette.pack(r, g, b);
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private static float smoothstep(float edge0, float edge1, float x) {
        float t = clamp01((x - edge0) / (edge1 - edge0));
        return t * t * (3.0f - 2.0f * t);
    }

    private static float clamp01(float v) {
        return v < 0.0f ? 0.0f : (v > 1.0f ? 1.0f : v);
    }
}
