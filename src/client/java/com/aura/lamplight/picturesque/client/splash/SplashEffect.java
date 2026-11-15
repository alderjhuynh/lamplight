package com.aura.lamplight.picturesque.client.splash;

import net.minecraft.core.BlockPos;

final class SplashEffect {
    static final float SPLASH_TICKS_PER_FRAME = 1.5F;
    static final float RIPPLE_TICKS_PER_FRAME = 2.5F;

    static final float SPLASH_LIFETIME = SplashTextures.SPLASH_FRAMES * SPLASH_TICKS_PER_FRAME;
    static final float RIPPLE_LIFETIME = SplashTextures.RIPPLE_FRAMES * RIPPLE_TICKS_PER_FRAME;

    final double x;
    final double y;
    final double z;
    final float scale;
    final boolean lava;
    final int tint;
    final BlockPos lightPos;

    int age;

    SplashEffect(double x, double y, double z, float scale, boolean lava, int tint, BlockPos lightPos) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.scale = scale;
        this.lava = lava;
        this.tint = tint;
        this.lightPos = lightPos;
    }

    float lifetime() {
        return this.lava ? SPLASH_LIFETIME : Math.max(SPLASH_LIFETIME, RIPPLE_LIFETIME);
    }

    boolean expired() {
        return this.age >= this.lifetime();
    }
}
