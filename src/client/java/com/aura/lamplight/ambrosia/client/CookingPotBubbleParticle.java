package com.aura.lamplight.ambrosia.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.util.RandomSource;

public class CookingPotBubbleParticle extends SingleQuadParticle {
    CookingPotBubbleParticle(ClientLevel level, double x, double y, double z,
                             double xd, double yd, double zd, TextureAtlasSprite sprite) {
        super(level, x, y, z, xd, yd, zd, sprite);
        this.quadSize = 0.04F + this.random.nextFloat() * 0.04F;
        this.lifetime = 20 + this.random.nextInt(20);
        this.gravity = 0.0F;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (--this.lifetime <= 0) {
            this.remove();
            return;
        }
        this.yd += 0.0002D;
        this.move(this.xd, this.yd, this.zd);
        this.xd *= 0.85D;
        this.yd *= 0.85D;
        this.zd *= 0.85D;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.bySprite(this.sprite);
    }

    public static class Provider implements ParticleProvider<ColorParticleOption> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(ColorParticleOption options, ClientLevel level, double x, double y, double z,
                                       double xd, double yd, double zd, RandomSource random) {
            CookingPotBubbleParticle particle = new CookingPotBubbleParticle(level, x, y, z, xd, yd, zd, this.sprites.get(random));
            particle.setColor(options.getRed(), options.getGreen(), options.getBlue());
            particle.setAlpha(options.getAlpha());
            return particle;
        }
    }
}
