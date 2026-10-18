package com.aura.lamplight.wayfarer.client.util;

public final class ColorUtil {
    private ColorUtil() {}

    public static int argb(int a, int r, int g, int b) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int rgb(int r, int g, int b) {
        return argb(0xFF, r, g, b);
    }

    public static int shadeRgb(int rgb, int shade) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        int mul = switch (shade & 3) {
            case 0 -> 180;
            case 1 -> 220;
            case 3 -> 135;
            default -> 255;
        };
        r = r * mul / 255;
        g = g * mul / 255;
        b = b * mul / 255;
        return rgb(r, g, b);
    }

    public static int brightnessRgb(int rgb, float mult) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        r = clamp((int) (r * mult));
        g = clamp((int) (g * mult));
        b = clamp((int) (b * mult));
        return rgb(r, g, b);
    }

    public static int desaturate(int rgb, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        int gray = (r + g + b) / 3;
        r = (int) (r + (gray - r) * t);
        g = (int) (g + (gray - g) * t);
        b = (int) (b + (gray - b) * t);
        return rgb(r, g, b);
    }

    public static int applyHillShade(int rgb, int vSlope, int dSlope, int mode) {
        if (mode <= 0) return rgb;
        if (mode == 1) {

            if (vSlope > 0) return brightnessRgb(rgb, 1.05f);
            if (vSlope < 0) return brightnessRgb(rgb, 0.95f);
            return rgb;
        }

        float crossZ = -vSlope;
        float crossX = vSlope - dSlope;
        float cos;
        if (crossZ < 1.0f) {
            if (vSlope == 1 && dSlope == 1) {
                cos = 1.0f;
            } else {
                float cast = 1.0f - crossZ;
                float mag = (float) Math.sqrt(crossX * crossX + 1.0f + crossZ * crossZ);
                cos = (float) (cast / mag / Math.sqrt(2.0));
            }
        } else {
            cos = 0f;
        }
        float maxDirect = 0.6666667f;
        float direct;
        if (cos == 1.0f) direct = maxDirect;
        else if (cos > 0) direct = (float) Math.ceil(cos * 10f) / 10f * maxDirect * 0.88388f;
        else direct = 0f;

        // Gentle relief: slopes whisper instead of brooding (~0.88..1.08).
        float mult = 0.88f + direct * 0.30f;
        return brightnessRgb(rgb, mult);
    }

    /**
     * Calm light multiplier from sky/block light levels (0..15 each).
     *
     * <p>Full sun reads ~1.0 while unlit corners stay readable at ~0.86 —
     * night on the map feels dusky, never brooding.
     */
    public static float calmLight(int sky, int block) {
        float min = 9f;
        float raw = (min + Math.max(sky, block)) / (15f + min);
        float t = Math.max(0f, Math.min(1f, (raw - 0.375f) / 0.625f));
        return 0.86f + 0.14f * t;
    }

    /**
     * Calm elevation multiplier. Higher ground lifts a breath, low ground
     * dips gently — the ordering cue survives without darkening oceans
     * and valleys into ink.
     */
    public static float calmDepth(int h, int slopeMode) {
        float norm = (h - 40f) / 88f;
        norm = Math.max(0f, Math.min(1f, norm));
        if (slopeMode >= 2) return 0.96f + 0.08f * norm;
        if (slopeMode == 1) return 0.94f + 0.12f * norm;
        return 0.97f + 0.06f * norm;
    }

    public static int lerp(int aRgb, int bRgb, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int ar = (aRgb >> 16) & 0xFF, ag = (aRgb >> 8) & 0xFF, ab = aRgb & 0xFF;
        int br = (bRgb >> 16) & 0xFF, bg = (bRgb >> 8) & 0xFF, bb = bRgb & 0xFF;
        return rgb((int) (ar + (br - ar) * t), (int) (ag + (bg - ag) * t), (int) (ab + (bb - ab) * t));
    }

    public static int biomeTint(int baseRgb, int tintRgb) {
        int br = (baseRgb >> 16) & 0xFF, bg = (baseRgb >> 8) & 0xFF, bb = baseRgb & 0xFF;
        int tr = (tintRgb >> 16) & 0xFF, tg = (tintRgb >> 8) & 0xFF, tb = tintRgb & 0xFF;
        int rr = br * tr / 255;
        int gg = bg * tg / 255;
        int bb2 = bb * tb / 255;
        return rgb(rr, gg, bb2);
    }

    private static int clamp(int v) { return Math.max(0, Math.min(255, v)); }
}
