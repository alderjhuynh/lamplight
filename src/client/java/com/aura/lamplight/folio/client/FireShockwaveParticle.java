package com.aura.lamplight.folio.client;

import com.aura.lamplight.folio.particle.FireShockwaveParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class FireShockwaveParticle extends SingleQuadParticle {

    private static final int LIFETIME = 10;

    private final float maxSize;
    private float previousSize;

    protected FireShockwaveParticle(
        final ClientLevel level,
        final double x,
        final double y,
        final double z,
        final float size,
        final SpriteSet sprites
    ) {
        super(level, x, y, z, sprites.first());
        this.maxSize = Mth.clamp(size, 0.25F, 12.0F);
        this.lifetime = LIFETIME;
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.quadSize = this.maxSize * 0.12F;
        this.previousSize = this.quadSize;
        this.rCol = 1.0F;
        this.gCol = 0.52F;
        this.bCol = 0.14F;
        this.alpha = 0.95F;
        this.roll = this.random.nextFloat() * 360.0F;
        this.oRoll = this.roll;
    }

    @Override
    public void tick() {
        this.previousSize = this.quadSize;

        float progress = (this.age + 1) / (float) LIFETIME;
        float inverse = 1.0F - progress;
        float eased = 1.0F - inverse * inverse * inverse;
        this.quadSize = this.maxSize * (0.12F + 0.88F * eased);
        this.alpha = 0.95F * inverse;

        super.tick();
    }

    @Override
    public float getQuadSize(final float partialTick) {
        return Mth.lerp(partialTick, this.previousSize, this.quadSize);
    }

    @Override
    protected SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    @Override
    protected int getLightCoords(final float partialTick) {
        return LightCoordsUtil.FULL_BRIGHT;
    }

    public static class Provider implements ParticleProvider<FireShockwaveParticleOptions> {
        private final SpriteSet sprites;

        public Provider(final SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(
            final FireShockwaveParticleOptions options,
            final ClientLevel level,
            final double x,
            final double y,
            final double z,
            final double xAux,
            final double yAux,
            final double zAux,
            final RandomSource random
        ) {
            return new FireShockwaveParticle(level, x, y, z, options.size(), this.sprites);
        }
    }
}
