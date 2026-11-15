package com.aura.lamplight.picturesque.client.splash;

import com.aura.lamplight.picturesque.Picturesque;
import net.minecraft.resources.Identifier;

/**
 * Texture ids and atlas layout for the splash art.
 *
 * <p>Every {@code splash}, {@code splash_rim} and {@code lava_splash} frame is a 48x28 box-UV net for a
 * 12 x 16 x 12 px box:
 * <pre>
 *   x:   0      12      24      36      48
 *   y 0  +-------+-------+-------+-------+
 *        |       |       | RING  |       |   12x12 footprint ring (the box "bottom" face)
 *   y 12 +-------+-------+-------+-------+
 *        | wall  | wall  | wall  | wall  |   four 12px wall bands, 16px tall, wave grows from the base
 *   y 28 +-------+-------+-------+-------+
 * </pre>
 * Ripples are a plain 16x16 quad.
 */
final class SplashTextures {
    private SplashTextures() {}

    static final int ATLAS_W = 48;
    static final int ATLAS_H = 28;

    /** Footprint / wall width of the box, in texels. */
    static final int BOX_W = 12;
    /** Wall height of the box, in texels. */
    static final int BOX_H = 16;
    /** First texel row of the wall bands. */
    static final int WALL_TOP_V = 12;
    /** Origin of the ring (footprint) region. */
    static final int RING_U = 24;

    static final int RIPPLE_FRAMES = 8;
    /** splash_rim / lava_splash have 13 frames, which is the full lifetime of a splash. */
    static final int SPLASH_FRAMES = 13;
    /** splash_0..9 carry pixels, splash_10..12 are blank; skip drawing the blank ones. */
    static final int SPLASH_BODY_FRAMES = 10;

    static final Identifier[] RIPPLE = frames("ripple", RIPPLE_FRAMES);
    static final Identifier[] SPLASH = frames("splash", SPLASH_FRAMES);
    static final Identifier[] RIM = frames("splash_rim", SPLASH_FRAMES);
    static final Identifier[] LAVA = frames("lava_splash", SPLASH_FRAMES);

    private static Identifier[] frames(String name, int count) {
        Identifier[] out = new Identifier[count];
        for (int i = 0; i < count; i++) {
            out[i] = Picturesque.id("textures/picturesque/splash/" + name + "_" + i + ".png");
        }
        return out;
    }
}
