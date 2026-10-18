package com.aura.lamplight.picturesque.client.color;

public record LightMood(
        int sunTint,
        int skyTint,
        int fogTint,
        int ambientTint,
        int shadowTint,
        float lanternBoost,
        float weatherDim) {

    public static LightMood forPhase(DayPhase phase, float weatherDim) {
        float dim = clamp01(weatherDim);
        return switch (phase) {
            case MORNING -> new LightMood(
                    PicturesquePalette.lerp(PicturesquePalette.SUN_BUTTER, PicturesquePalette.EDGE_CORAL, 0.35f * (1.0f - dim)),
                    PicturesquePalette.FOG_LAVENDER_MORNING,
                    PicturesquePalette.FOG_LAVENDER_MORNING,
                    PicturesquePalette.lerp(PicturesquePalette.SUN_BUTTER, PicturesquePalette.SHADOW_PERIWINKLE, 0.25f + 0.35f * dim),
                    PicturesquePalette.SHADOW_PERIWINKLE,
                    0.35f,
                    dim);
            case DAY -> new LightMood(
                    PicturesquePalette.SUN_BUTTER,
                    PicturesquePalette.SKY_DAY,
                    PicturesquePalette.FOG_HONEY_NOON,
                    PicturesquePalette.lerp(PicturesquePalette.BOUNCE_GRASS, PicturesquePalette.SUN_BUTTER, 0.55f),
                    PicturesquePalette.SHADOW_PERIWINKLE,
                    0.0f,
                    dim);
            case GOLDEN -> new LightMood(
                    PicturesquePalette.SUN_HONEY,
                    PicturesquePalette.lerp(PicturesquePalette.SKY_DAY, PicturesquePalette.SKY_GOLDEN_RIM, 0.65f),
                    PicturesquePalette.lerp(PicturesquePalette.FOG_HONEY_NOON, PicturesquePalette.FOG_PEACH_DUSK, 0.55f),
                    PicturesquePalette.lerp(PicturesquePalette.SUN_HONEY, PicturesquePalette.EDGE_CORAL, 0.35f),
                    PicturesquePalette.SHADOW_MAUVE,
                    0.25f,
                    dim);
            case DUSK -> new LightMood(
                    PicturesquePalette.lerp(PicturesquePalette.EDGE_CORAL, PicturesquePalette.EDGE_ROSY_LAVENDER, 0.5f),
                    PicturesquePalette.lerp(PicturesquePalette.EDGE_CORAL, PicturesquePalette.EDGE_ROSY_LAVENDER, 0.55f),
                    PicturesquePalette.FOG_PEACH_DUSK,
                    PicturesquePalette.lerp(PicturesquePalette.EDGE_CORAL, PicturesquePalette.SHADOW_MAUVE, 0.45f),
                    PicturesquePalette.SHADOW_MAUVE,
                    0.6f,
                    dim);
            case NIGHT -> new LightMood(
                    PicturesquePalette.MOON_SILVER,
                    PicturesquePalette.NIGHT_INDIGO,
                    PicturesquePalette.NIGHT_INDIGO,
                    PicturesquePalette.lerp(PicturesquePalette.NIGHT_INDIGO, PicturesquePalette.LANTERN_AMBER, 0.25f),
                    PicturesquePalette.SHADOW_PERIWINKLE,
                    1.0f,
                    dim);
        };
    }

    private static float clamp01(float v) {
        return v < 0.0f ? 0.0f : (v > 1.0f ? 1.0f : v);
    }
}
