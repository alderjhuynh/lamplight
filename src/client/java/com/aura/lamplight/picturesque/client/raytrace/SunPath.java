package com.aura.lamplight.picturesque.client.raytrace;

public final class SunPath {
    private SunPath() {
    }

    private static final float TILT_Z = 0.35f;

    public static void sunDirection(long dayTime, float[] out) {
        float frac = dayFraction(dayTime);
        float theta = frac * (float) (Math.PI * 2.0);
        float x = -(float) Math.cos(theta);
        float y = (float) Math.sin(theta);
        float z = TILT_Z;
        float len = (float) Math.sqrt(x * x + y * y + z * z);
        out[0] = x / len;
        out[1] = y / len;
        out[2] = z / len;
    }

    public static float daylightFactor(long dayTime) {
        return smoothstep(-0.06f, 0.14f, sunElevation(dayTime));
    }

    public static float sunElevation(long dayTime) {
        float frac = dayFraction(dayTime);
        return (float) Math.sin(frac * (float) (Math.PI * 2.0));
    }

    public static float dayFraction(long dayTime) {
        long t = ((dayTime % 24000L) + 24000L) % 24000L;
        return t / 24000.0f;
    }

    public static float wrapDiffuse(float nDotL, float wrap) {
        float w = wrap < 0.0f ? 0.0f : (wrap > 1.0f ? 1.0f : wrap);
        float v = (nDotL + w) / (1.0f + w);
        return v < 0.0f ? 0.0f : (v > 1.0f ? 1.0f : v);
    }

    private static float smoothstep(float edge0, float edge1, float x) {
        float t = (x - edge0) / (edge1 - edge0);
        t = t < 0.0f ? 0.0f : (t > 1.0f ? 1.0f : t);
        return t * t * (3.0f - 2.0f * t);
    }
}
