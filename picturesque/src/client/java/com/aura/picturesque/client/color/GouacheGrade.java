package com.aura.picturesque.client.color;

/**
 * The gouache grade: pigment-on-paper color math applied late to fog, sky,
 * and light tints (and one day to a full post pass).
 *
 * <p>Goals from the vision: lift blacks to plum/navy instead of crushing,
 * soften only near-white brights, enrich midtones and pastels, roll brights
 * off with a gentle filmic shoulder, and tint shadows periwinkle/mauve so
 * creases stay cozy instead of gray. Deliberately avoids hard contrast,
 * sharpening, and grain.
 *
 * <p>Vantage's per-biome pastel colors lead: the grade never repaints hue,
 * and saturation work is shaped so pastel fog/sky/grass (luma ~0.6-0.85)
 * comes out richer, not duller. Only near-blown highlights lose a little
 * saturation, like pigment thinning at the paper's edge.
 *
 * <p>All methods are pure float math on 0..1 channels so they can run on
 * packed ints, lightmap vectors, or a future GPU pass without change.
 */
public final class GouacheGrade {
    private GouacheGrade() {
    }

    /**
     * Grades one linear RGB pixel in place.
     *
     * @param r        red 0..1
     * @param g        green 0..1
     * @param b        blue 0..1
     * @param strength 0 = bypass, 1 = full picturesque grade
     * @param out      length-3 output array
     */
    public static void grade(float r, float g, float b, float strength, float[] out) {
        float s = clamp01(strength);

        // Gentle filmic shoulder: brights roll off like watercolor.
        float shoulder = 0.22f * s;
        float x = filmic(r, shoulder);
        float y = filmic(g, shoulder);
        float z = filmic(b, shoulder);
        // Renormalize so mid-gray stays roughly put.
        float norm = filmic(1.0f, shoulder);
        x /= norm;
        y /= norm;
        z /= norm;

        float luma = 0.2126f * x + 0.7152f * y + 0.0722f * z;

        // Near-whites only: soften like pigment thinning at the paper's edge.
        // Pastels (luma ~0.6-0.85) are left alone — Vantage owns that range.
        float hiWeight = smoothstep(0.80f, 1.0f, luma) * 0.25f * s;
        x = lerp(x, luma, hiWeight);
        y = lerp(y, luma, hiWeight);
        z = lerp(z, luma, hiWeight);

        // Midtones and pastels: rich, like pigment. The bell reaches up
        // through the pastel range so biome colors come out deeper, not duller.
        float midWeight = bell((luma - 0.55f) / 0.45f) * 0.22f * s;
        float sat = 1.0f + midWeight;
        luma = 0.2126f * x + 0.7152f * y + 0.0722f * z;
        x = lerp(luma, x, sat);
        y = lerp(luma, y, sat);
        z = lerp(luma, z, sat);

        // Blacks: lift toward plum/navy, never crushed.
        float darkWeight = (1.0f - smoothstep(0.0f, 0.35f, luma)) * 0.35f * s;
        float liftR = PicturesquePalette.red(PicturesquePalette.LIFT_PLUM) * 0.5f
                + PicturesquePalette.red(PicturesquePalette.LIFT_NAVY) * 0.5f;
        float liftG = PicturesquePalette.green(PicturesquePalette.LIFT_PLUM) * 0.5f
                + PicturesquePalette.green(PicturesquePalette.LIFT_NAVY) * 0.5f;
        float liftB = PicturesquePalette.blue(PicturesquePalette.LIFT_PLUM) * 0.5f
                + PicturesquePalette.blue(PicturesquePalette.LIFT_NAVY) * 0.5f;
        x = lerp(x, liftR, darkWeight * (1.0f - x));
        y = lerp(y, liftG, darkWeight * (1.0f - y));
        z = lerp(z, liftB, darkWeight * (1.0f - z));

        // Shadows: whisper of periwinkle/mauve colored-pencil shading.
        float shadowWeight = (1.0f - smoothstep(0.0f, 0.5f, luma)) * 0.10f * s;
        float tintR = (PicturesquePalette.red(PicturesquePalette.SHADOW_PERIWINKLE)
                + PicturesquePalette.red(PicturesquePalette.SHADOW_MAUVE)) * 0.5f;
        float tintG = (PicturesquePalette.green(PicturesquePalette.SHADOW_PERIWINKLE)
                + PicturesquePalette.green(PicturesquePalette.SHADOW_MAUVE)) * 0.5f;
        float tintB = (PicturesquePalette.blue(PicturesquePalette.SHADOW_PERIWINKLE)
                + PicturesquePalette.blue(PicturesquePalette.SHADOW_MAUVE)) * 0.5f;
        x = lerp(x, tintR, shadowWeight);
        y = lerp(y, tintG, shadowWeight);
        z = lerp(z, tintB, shadowWeight);

        // Blend back toward the input so strength acts as an opacity.
        out[0] = lerp(r, clamp01(x), s);
        out[1] = lerp(g, clamp01(y), s);
        out[2] = lerp(b, clamp01(z), s);
    }

    /** Grades a packed {@code 0xRRGGBB} color. Alpha is preserved separately by callers. */
    public static int gradePacked(int packed, float strength) {
        if (strength <= 0.001f) {
            return packed;
        }
        float[] out = new float[3];
        grade(PicturesquePalette.red(packed), PicturesquePalette.green(packed),
                PicturesquePalette.blue(packed), strength, out);
        return PicturesquePalette.pack(out[0], out[1], out[2]);
    }

    private static float filmic(float x, float shoulder) {
        return x * (1.0f + shoulder) / (1.0f + shoulder * x);
    }

    private static float smoothstep(float edge0, float edge1, float x) {
        float t = clamp01((x - edge0) / (edge1 - edge0));
        return t * t * (3.0f - 2.0f * t);
    }

    private static float bell(float x) {
        return (float) Math.exp(-x * x);
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private static float clamp01(float v) {
        return v < 0.0f ? 0.0f : (v > 1.0f ? 1.0f : v);
    }
}
