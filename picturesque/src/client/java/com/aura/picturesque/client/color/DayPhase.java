package com.aura.picturesque.client.color;

/**
 * Coarse time-of-day buckets driving the gouache grade.
 *
 * <p>Vanilla {@code dayTime % 24000}: 0 is sunrise, 6000 noon, 12000 sunset,
 * 18000 midnight. Buckets are deliberately wide and soft: Picturesque blends
 * between them rather than snapping, so the day drifts butter -&gt; honey -&gt;
 * coral -&gt; rosy-lavender instead of jumping.
 */
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

    /** 0..1 edge-of-day factor: 1 at deep night, 0 at noon. Used for lantern emphasis. */
    public static float nightFactor(long dayTime) {
        long t = ((dayTime % 24000L) + 24000L) % 24000L;
        if (t >= 14000L && t < 23000L) {
            return 1.0f;
        }
        if (t >= 12000L) {
            return (t - 12000L) / 2000.0f;
        }
        if (t < 2500L) {
            // Dawn tail: still a little dusky just after wrap.
            return 1.0f - (t + 1000L) / 3500.0f;
        }
        return 0.0f;
    }
}
