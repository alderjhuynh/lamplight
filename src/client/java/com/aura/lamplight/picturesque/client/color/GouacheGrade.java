package com.aura.lamplight.picturesque.client.color;

public final class GouacheGrade {
    private GouacheGrade() {
    }

    public static void grade(float r, float g, float b, float strength, float[] out) {
        float s = clamp01(strength);

        float shoulder = 0.22f * s;
        float x = filmic(r, shoulder);
        float y = filmic(g, shoulder);
        float z = filmic(b, shoulder);
        float norm = filmic(1.0f, shoulder);
        x /= norm;
        y /= norm;
        z /= norm;

        float luma = 0.2126f * x + 0.7152f * y + 0.0722f * z;

        float hiWeight = smoothstep(0.80f, 1.0f, luma) * 0.25f * s;
        x = lerp(x, luma, hiWeight);
        y = lerp(y, luma, hiWeight);
        z = lerp(z, luma, hiWeight);

        float midWeight = bell((luma - 0.55f) / 0.45f) * 0.22f * s;
        float sat = 1.0f + midWeight;
        luma = 0.2126f * x + 0.7152f * y + 0.0722f * z;
        x = lerp(luma, x, sat);
        y = lerp(luma, y, sat);
        z = lerp(luma, z, sat);

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

        out[0] = lerp(r, clamp01(x), s);
        out[1] = lerp(g, clamp01(y), s);
        out[2] = lerp(b, clamp01(z), s);
    }

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
