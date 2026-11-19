package com.aura.lamplight.folio.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public class ChainLinkParticle extends SingleQuadParticle {
    protected ChainLinkParticle(ClientLevel level, double x, double y, double z,
                                double xd, double yd, double zd, SpriteSet sprites) {
        super(level, x, y, z, xd, yd, zd, sprites.first());
        this.lifetime = 8;
        this.quadSize = 0.14F;
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.rCol = 0.6F;
        this.gCol = 0.9F;
        this.bCol = 1.0F;
        this.alpha = 0.85F;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double xd, double yd, double zd, RandomSource random) {
            return new ChainLinkParticle(level, x, y, z, xd, yd, zd, this.sprites);
        }
    }
}
