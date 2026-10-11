package com.aura.menagerie.crow;

public final class CrowMaths {
    public static final float STARTING_ANGLE = 0.0174532925F;
    public static final float EIGHT_STARTING_ANGLE = STARTING_ANGLE * 8;

    private CrowMaths() {
    }

    public static float rad(double deg) {
        return (float) Math.toRadians(deg);
    }
}
