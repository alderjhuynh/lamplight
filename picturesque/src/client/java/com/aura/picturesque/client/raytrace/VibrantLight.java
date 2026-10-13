package com.aura.picturesque.client.raytrace;

public final class VibrantLight {
    private VibrantLight() {
    }

    public static void saturate(float r, float g, float b, float amount, float[] out) {
        float a = amount < 0.0f ? 0.0f : (amount > 1.0f ? 1.0f : amount);
        if (a <= 0.001f) {
            out[0] = r;
            out[1] = g;
            out[2] = b;
            return;
        }
        float luma = 0.2126f * r + 0.7152f * g + 0.0722f * b;
        float bell = (float) Math.exp(-Math.pow((luma - 0.5f) / 0.42f, 2.0));
        float sat = 1.0f + bell * 0.55f * a;
        out[0] = clamp01(luma + (r - luma) * sat);
        out[1] = clamp01(luma + (g - luma) * sat);
        out[2] = clamp01(luma + (b - luma) * sat);
    }

    public static int saturatePacked(int packed, float amount) {
        float[] out = new float[3];
        saturate(red(packed), green(packed), blue(packed), amount, out);
        return pack(out[0], out[1], out[2]);
    }

    public static void realisticContrast(float r, float g, float b, float amount, float[] out) {
        float a = (amount < 0.0f ? 0.0f : (amount > 1.0f ? 1.0f : amount)) * 0.35f;
        out[0] = clamp01(sCurve(r, a));
        out[1] = clamp01(sCurve(g, a));
        out[2] = clamp01(sCurve(b, a));
    }

    public static int sinkingSun(int noonTint, int horizonTint, float sink) {
        float t = sink < 0.0f ? 0.0f : (sink > 1.0f ? 1.0f : sink);
        t = t * t * (3.0f - 2.0f * t);
        float r = red(noonTint) + (red(horizonTint) - red(noonTint)) * t;
        float g = green(noonTint) + (green(horizonTint) - green(noonTint)) * t;
        float b = blue(noonTint) + (blue(horizonTint) - blue(noonTint)) * t;
        return pack(r, g, b);
    }

    private static float sCurve(float x, float a) {
        float s = x * x * (3.0f - 2.0f * x);
        return x + (s - x) * a * 2.0f;
    }

    public static float red(int packed) {
        return ((packed >> 16) & 0xFF) / 255.0f;
    }

    public static float green(int packed) {
        return ((packed >> 8) & 0xFF) / 255.0f;
    }

    public static float blue(int packed) {
        return (packed & 0xFF) / 255.0f;
    }

    public static int pack(float r, float g, float b) {
        int ri = Math.round(clamp01(r) * 255.0f);
        int gi = Math.round(clamp01(g) * 255.0f);
        int bi = Math.round(clamp01(b) * 255.0f);
        return (ri << 16) | (gi << 8) | bi;
    }

    private static float clamp01(float v) {
        return v < 0.0f ? 0.0f : (v > 1.0f ? 1.0f : v);
    }
}
