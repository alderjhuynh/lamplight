package com.aura.lamplight.folio.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BaseAshSmokeParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;

public class FierySmokeParticle extends BaseAshSmokeParticle {

    private static final float SCALE = 1.6F;

    protected FierySmokeParticle(
        final ClientLevel level,
        final double x,
        final double y,
        final double z,
        final double xa,
        final double ya,
        final double za,
        final SpriteSet sprites
    ) {
        super(level, x, y, z, 0.1F, 0.1F, 0.1F, xa, ya, za, SCALE, sprites, 0.3F, 8, -0.1F, true);
        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 1.0F;
    }

    @Override
    public int getLightCoords(final float partialTick) {
        float hot = 1.0F - (this.age + partialTick) / this.lifetime;
        return LightCoordsUtil.addSmoothBlockEmission(super.getLightCoords(partialTick), hot);
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(final SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(
            final SimpleParticleType options,
            final ClientLevel level,
            final double x,
            final double y,
            final double z,
            final double xAux,
            final double yAux,
            final double zAux,
            final RandomSource random
        ) {
            return new FierySmokeParticle(level, x, y, z, xAux, yAux, zAux, this.sprites);
        }
    }
}
