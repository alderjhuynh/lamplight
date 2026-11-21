package com.aura.lamplight.folio.client;

import com.aura.lamplight.folio.entity.FireballEntity;
import com.aura.lamplight.folio.particle.FlameChargeParticleOptions;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.joml.Quaternionf;

public class FlameOrbParticle extends SingleQuadParticle {
     private static final float FRAMES_PER_TICK = 1.5F;
      private static final Map<Integer, FlameOrbParticle> ACTIVE = new HashMap<>();
      private static final int TRAIL_LENGTH = 6;

    private final int entityId;
    private final SpriteSet sprites;
    private final Deque<Vec3> trail = new ArrayDeque<>();
    private float size;
    private float oSize;
   private float drawScale = 1.0F;

    protected FlameOrbParticle(
        final ClientLevel level,
        final double x,
        final double y,
        final double z,
        final int entityId,
        final SpriteSet sprites
    ) {
        super(level, x, y, z, sprites.first());
        this.entityId = entityId;
        this.sprites = sprites;
        this.hasPhysics = false;
        this.gravity = 0.0F;
        this.lifetime = Integer.MAX_VALUE;
        this.size = FireballEntity.visualSize(0.0F);
        this.oSize = this.size;
        this.quadSize = this.size;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.oSize = this.size;

        if (!(this.level.getEntity(this.entityId) instanceof FireballEntity fireball) || !fireball.isAlive()) {
            this.remove();
            return;
        }
        this.age++;

        float target = FireballEntity.visualSize(fireball.charge());
        this.size = this.age == 1 ? target : Mth.lerp(0.35F, this.size, target);

        Vec3 center = this.center(fireball, 1.0F);
        this.setPos(center.x, center.y, center.z);

        if (fireball.isLaunched()) {
            this.trail.addFirst(center);
            while (this.trail.size() > TRAIL_LENGTH) {
                this.trail.removeLast();
            }
        } else {
            this.trail.clear();
        }

        this.spawnSparks(fireball, center);
    }

    private Vec3 center(final FireballEntity fireball, final float partialTick) {
        return fireball.getPosition(partialTick).add(0.0D, fireball.getBbHeight() / 2.0D, 0.0D);
    }

    private void spawnSparks(final FireballEntity fireball, final Vec3 center) {
        float charge = fireball.charge();
        double radius = this.size * 0.55D;

        int embers = 1 + (int) (charge * 2.0F) + (fireball.isLaunched() ? 1 : 0);
        for (int i = 0; i < embers; i++) {
            this.level.addParticle(ParticleTypes.FLAME,
                center.x + (this.random.nextDouble() - 0.5D) * 2.0D * radius,
                center.y + (this.random.nextDouble() - 0.5D) * 2.0D * radius,
                center.z + (this.random.nextDouble() - 0.5D) * 2.0D * radius,
                (this.random.nextDouble() - 0.5D) * 0.03D,
                0.02D + this.random.nextDouble() * 0.04D,
                (this.random.nextDouble() - 0.5D) * 0.03D);
        }
        if (this.random.nextInt(4) == 0) {
            this.level.addParticle(ParticleTypes.SMOKE,
                center.x + (this.random.nextDouble() - 0.5D) * radius,
                center.y + radius * 0.6D,
                center.z + (this.random.nextDouble() - 0.5D) * radius,
                0.0D, 0.04D, 0.0D);
        }
        if (fireball.isLaunched()) {
            Vec3 motion = fireball.getDeltaMovement();
            Vec3 direction = motion.lengthSqr() > 1.0E-6D ? motion.normalize() : new Vec3(0.0D, 0.0D, -1.0D);
            Vec3 side = direction.cross(new Vec3(0.0D, 1.0D, 0.0D));
            if (side.lengthSqr() < 1.0E-6D) {
                side = direction.cross(new Vec3(1.0D, 0.0D, 0.0D));
            }
            side = side.normalize();
            Vec3 up = side.cross(direction).normalize();
            Vec3 back = direction.scale(-this.size * 0.9D);

            double angle = this.random.nextDouble() * Math.PI * 2.0D;
            double trailRadius = this.size * (0.55D + this.random.nextDouble() * 0.35D);
            Vec3 offset = side.scale(Math.cos(angle) * trailRadius).add(up.scale(Math.sin(angle) * trailRadius));

            this.level.addParticle(ParticleTypes.FLAME,
                center.x + back.x + offset.x, center.y + back.y + offset.y, center.z + back.z + offset.z,
                offset.x * 0.12D - direction.x * 0.05D, 0.02D, offset.z * 0.12D - direction.z * 0.05D);

            if (this.random.nextInt(2) == 0) {
                this.level.addParticle(ParticleTypes.LARGE_SMOKE,
                    center.x + back.x, center.y + back.y, center.z + back.z,
                    0.0D, 0.03D, 0.0D);
            }
            if (this.random.nextInt(3) == 0) {
                this.level.addParticle(ParticleTypes.LAVA,
                    center.x + back.x + offset.x * 0.5D, center.y + back.y + offset.y * 0.5D,
                    center.z + back.z + offset.z * 0.5D, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    protected void extractRotatedQuad(
        final QuadParticleRenderState state,
        final Camera camera,
        final Quaternionf rotation,
        final float partialTick
    ) {
        if (!(this.level.getEntity(this.entityId) instanceof FireballEntity fireball)) {
            return;
        }
        Vec3 center = this.center(fireball, partialTick);
        Vec3 cam = camera.position();

        int frame = Math.floorMod((int) ((this.age + partialTick) * FRAMES_PER_TICK), FlameChargeParticleOptions.FRAMES);
        this.sprite = this.sprites.get(frame, FlameChargeParticleOptions.FRAMES - 1);

        this.drawScale = 1.0F;
        this.extractRotatedQuad(state, rotation,
            (float) (center.x - cam.x), (float) (center.y - cam.y), (float) (center.z - cam.z), partialTick);

        if (fireball.isLaunched() && !this.trail.isEmpty()) {
            float baseAlpha = this.alpha;
            float baseRed = this.rCol;
            float baseGreen = this.gCol;
            float baseBlue = this.bCol;

            int index = 0;
            for (Vec3 point : this.trail) {
                float fade = (index + 1.0F) / this.trail.size();

               this.sprite = this.sprites.get(
                    Math.floorMod(frame - (index + 1) * 2, FlameChargeParticleOptions.FRAMES),
                    FlameChargeParticleOptions.FRAMES - 1);
                this.drawScale = 1.0F - 0.58F * fade;
                this.alpha = baseAlpha * (1.0F - 0.8F * fade) * 0.7F;
                this.rCol = 1.0F;
                this.gCol = 0.62F - 0.25F * fade;
                this.bCol = 0.22F - 0.16F * fade;

                this.extractRotatedQuad(state, rotation,
                    (float) (point.x - cam.x), (float) (point.y - cam.y), (float) (point.z - cam.z),
                    partialTick);
                index++;
            }

            this.drawScale = 1.0F;
            this.alpha = baseAlpha;
            this.rCol = baseRed;
            this.gCol = baseGreen;
            this.bCol = baseBlue;
        }
    }

    @Override
    public float getQuadSize(final float partialTick) {
        float pulse = 1.0F + 0.04F * Mth.sin((this.age + partialTick) * 0.7F);
        return Mth.lerp(partialTick, this.oSize, this.size) * pulse * this.drawScale;
    }

    @Override
    protected int getLightCoords(final float partialTick) {
        return LightCoordsUtil.FULL_BRIGHT;
    }

    @Override
    protected SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    @Override
    public void remove() {
        ACTIVE.remove(this.entityId, this);
        super.remove();
    }

    public static class Provider implements ParticleProvider<FlameChargeParticleOptions> {
        private final SpriteSet sprites;

        public Provider(final SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public @Nullable Particle createParticle(
            final FlameChargeParticleOptions options,
            final ClientLevel level,
            final double x,
            final double y,
            final double z,
            final double xAux,
            final double yAux,
            final double zAux,
            final RandomSource random
        ) {
            FlameOrbParticle existing = ACTIVE.get(options.entityId());
            if (existing != null && existing.isAlive() && existing.level == level) {
                return null;
            }
            FlameOrbParticle particle = new FlameOrbParticle(level, x, y, z, options.entityId(), this.sprites);
            ACTIVE.put(options.entityId(), particle);
            return particle;
        }
    }
}
