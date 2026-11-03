package com.aura.lamplight.wayfarer.client.waystone;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TrialSpawnerDetectionParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public class WaystoneTeleportParticle extends TrialSpawnerDetectionParticle {
    protected WaystoneTeleportParticle(ClientLevel level, double x, double y, double z,
                                       double xd, double yd, double zd, float quadSize, SpriteSet sprites) {
        super(level, x, y, z, xd, yd, zd, quadSize, sprites);
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double xd, double yd, double zd, RandomSource random) {
            return new WaystoneTeleportParticle(level, x, y, z, xd, yd, zd, 1.5F, this.sprites);
        }
    }
}
