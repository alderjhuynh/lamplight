package com.aura.lamplight.folio.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public class GravityWellParticle extends SingleQuadParticle {
    protected GravityWellParticle(ClientLevel level, double x, double y, double z,
                                  double xd, double yd, double zd, SpriteSet sprites) {
        super(level, x, y, z, xd, yd, zd, sprites.first());
        this.lifetime = 24;
        this.quadSize = 0.22F;
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.rCol = 0.72F;
        this.gCol = 0.45F;
        this.bCol = 1.0F;
        this.alpha = 0.9F;
    }

    @Override
    public void tick() {
        this.xd = 0.0D;
        this.yd = 0.0D;
        this.zd = 0.0D;
        super.tick();
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
            return new GravityWellParticle(level, x, y, z, xd, yd, zd, this.sprites);
        }
    }
}
