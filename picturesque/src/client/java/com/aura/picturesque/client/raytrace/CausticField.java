package com.aura.picturesque.client.raytrace;

public final class CausticField {
    private CausticField() {
    }

    public static float shimmer(double x, double y, double z, double timeSeconds) {
        return pattern(x, y, z, timeSeconds * 1.6);
    }

    public static float staticPattern(double x, double y, double z) {
        return pattern(x, y, z, 0.7);
    }

    private static float pattern(double x, double y, double z, double t) {
        double w1 = Math.sin(x * 1.7 + t * 1.3) + Math.sin((y * 0.8 + z * 1.3) * 1.9 - t * 1.1);
        double w2 = Math.sin((x * 0.6 - z * 1.1) * 2.3 + t * 0.9) + Math.sin(y * 2.1 - t * 1.7);
        double w3 = Math.sin((x + y * 1.3 + z * 0.7) * 1.2 + t * 0.7);
        double v = (w1 + w2 * 0.7 + w3 * 0.5) / 5.4 + 0.55;
        v = v * v * (3.0 - 2.0 * v);
        if (v < 0.0) {
            return 0.0f;
        }
        return (float) Math.min(1.0, v * 1.15);
    }
}
