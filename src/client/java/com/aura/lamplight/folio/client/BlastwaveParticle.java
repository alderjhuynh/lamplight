package com.aura.lamplight.folio.client;

import com.aura.lamplight.folio.particle.BlastwaveParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class BlastwaveParticle extends SingleQuadParticle {

    private static final int LIFETIME = 10;

    private static final float TINT_R = 1.0F;
    private static final float TINT_G = 0.52F;
    private static final float TINT_B = 0.14F;

    private static final float START_ALPHA = 0.95F;

    private final SpriteSet sprites;

    protected BlastwaveParticle(
        final ClientLevel level,
        final double x,
        final double y,
        final double z,
        final float size,
        final SpriteSet sprites
    ) {
        super(level, x, y, z, sprites.first());
        this.sprites = sprites;
        this.quadSize = Mth.clamp(size, 0.25F, 12.0F);
        this.lifetime = LIFETIME;
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.rCol = TINT_R;
        this.gCol = TINT_G;
        this.bCol = TINT_B;
        this.alpha = START_ALPHA;
        this.roll = this.random.nextFloat() * Mth.TWO_PI;
        this.oRoll = this.roll;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);

        float progress = this.age / (float) this.lifetime;
        float fade = Mth.clamp((1.0F - progress) / 0.4F, 0.0F, 1.0F);
        this.alpha = START_ALPHA * fade;
    }

    @Override
    protected SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    @Override
    protected int getLightCoords(final float partialTick) {
        return LightCoordsUtil.FULL_BRIGHT;
    }

    public static class Provider implements ParticleProvider<BlastwaveParticleOptions> {
        private final SpriteSet sprites;

        public Provider(final SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(
            final BlastwaveParticleOptions options,
            final ClientLevel level,
            final double x,
            final double y,
            final double z,
            final double xAux,
            final double yAux,
            final double zAux,
            final RandomSource random
        ) {
            return new BlastwaveParticle(level, x, y, z, options.size(), this.sprites);
        }
    }
}
