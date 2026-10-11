package com.aura.picturesque.client.color;

/**
 * Central gouache palette for Picturesque.
 *
 * <p>All colors are packed {@code 0xRRGGBB} ints. Values are chosen from the
 * mod vision: buttery peach-gold sun, honey/coral/rosy-lavender day edges,
 * periwinkle and dusty-mauve shadows instead of gray, plum/navy lifted
 * blacks, indigo night with silvery moonlight, generous amber lantern glow,
 * and aqua-to-lilac jelly water.
 */
public final class PicturesquePalette {
    private PicturesquePalette() {
    }

    // Sunlight through the day.
    public static final int SUN_BUTTER = 0xFFD9A3;
    public static final int SUN_HONEY = 0xF2B56B;
    public static final int EDGE_CORAL = 0xF2917E;
    public static final int EDGE_ROSY_LAVENDER = 0xD9A7C7;

    // Colored-pencil shading: never plain gray or black.
    public static final int SHADOW_PERIWINKLE = 0x8E9BC8;
    public static final int SHADOW_MAUVE = 0xB08FA8;

    // Lifted blacks: deep plum or navy instead of crushed black.
    public static final int LIFT_PLUM = 0x2A2138;
    public static final int LIFT_NAVY = 0x232C4A;

    // Night: deep indigo sky, silvery-periwinkle moon.
    public static final int NIGHT_INDIGO = 0x1B2340;
    public static final int MOON_SILVER = 0xC9D4F2;

    // Interiors: the real star. Generous amber pools, never cold white.
    public static final int LANTERN_AMBER = 0xFFB45E;
    public static final int LANTERN_DEEP = 0xE07B39;

    // Exaggerated bounce: grass blushes onto stone, mushroom houses warm snow.
    public static final int BOUNCE_GRASS = 0x7CC46A;
    public static final int BOUNCE_WARM_RED = 0xD86A5A;

    // Water: translucent aqua-to-lilac jelly, never mirror-sharp.
    public static final int WATER_AQUA = 0x6FD8D0;
    public static final int WATER_LILAC = 0xB9A7E6;

    // Fog by mood: lavender mornings, peachy dusks, minty forests.
    public static final int FOG_LAVENDER_MORNING = 0xCFC3E8;
    public static final int FOG_PEACH_DUSK = 0xF5C39A;
    public static final int FOG_MINT_FOREST = 0xBFE3C8;
    public static final int FOG_HONEY_NOON = 0xE8DCC8;

    // Marshmallow-cloud sky rims.
    public static final int SKY_DAY = 0xA8CCEE;
    public static final int SKY_GOLDEN_RIM = 0xFFC98A;
    public static final int CLOUD_BACKLIT = 0xFFF1DC;

    // Golden light shafts and floating motes.
    public static final int MOTE_GOLD = 0xFFD98A;

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

    /** Linear interpolate between two packed colors. {@code t} is clamped 0..1. */
    public static int lerp(int a, int b, float t) {
        float k = clamp01(t);
        float r = red(a) + (red(b) - red(a)) * k;
        float g = green(a) + (green(b) - green(a)) * k;
        float bl = blue(a) + (blue(b) - blue(a)) * k;
        return pack(r, g, bl);
    }

    /** Multiply a packed color by a scalar, clamping to gamut. */
    public static int scale(int packed, float s) {
        return pack(red(packed) * s, green(packed) * s, blue(packed) * s);
    }

    static float clamp01(float v) {
        return v < 0.0f ? 0.0f : (v > 1.0f ? 1.0f : v);
    }
}
