package com.aura.picturesque.client.color;

public enum DayPhase {
    MORNING,
    DAY,
    GOLDEN,
    DUSK,
    NIGHT;

    public static DayPhase forTime(long dayTime) {
        long t = ((dayTime % 24000L) + 24000L) % 24000L;
        if (t < 2500L || t >= 23000L) {
            return MORNING;
        }
        if (t < 9000L) {
            return DAY;
        }
        if (t < 11500L) {
            return GOLDEN;
        }
        if (t < 14000L) {
            return DUSK;
        }
        return NIGHT;
    }

    public static float nightFactor(long dayTime) {
        long t = ((dayTime % 24000L) + 24000L) % 24000L;
        if (t >= 14000L && t < 23000L) {
            return 1.0f;
        }
        if (t >= 12000L) {
            return (t - 12000L) / 2000.0f;
        }
        if (t < 2500L) {
            return 1.0f - (t + 1000L) / 3500.0f;
        }
        return 0.0f;
    }
}
