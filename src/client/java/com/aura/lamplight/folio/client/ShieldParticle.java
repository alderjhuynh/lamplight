package com.aura.lamplight.folio.client;

import com.aura.lamplight.folio.particle.ShieldParticleOptions;
import com.aura.lamplight.folio.spell.ShieldGeometry;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

public class ShieldParticle extends SingleQuadParticle {

    private static final Map<Integer, ShieldParticle> ACTIVE = new HashMap<>();

    private static final int HEARTBEAT_TIMEOUT = 10;
    private static final int APPEAR_TICKS = 5;
    private static final int COLLAPSE_TICKS = 6;

    private static final float OUTER_ALPHA = 0.8F;
    private static final float INNER_ALPHA = 0.6F;
    private static final float INNER_ALPHA_FIRST_PERSON = 0.18F;
    private static final float SPIN_PER_TICK = 0.03F;

    private final int entityId;

    private int lastHeartbeat;
    private int collapseTicks = -1;
    private float pulse;
    private float oPulse;

    private float yRot;
    private float xRot;

    protected ShieldParticle(
        final ClientLevel level,
        final double x,
        final double y,
        final double z,
        final int entityId,
        final SpriteSet sprites
    ) {
        super(level, x, y, z, sprites.first());
        this.entityId = entityId;
        this.lifetime = Integer.MAX_VALUE;
        this.quadSize = (float) ShieldGeometry.RADIUS;
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.rCol = 1.0F;
        this.gCol = 0.95F;
        this.bCol = 0.75F;
        this.roll = this.random.nextFloat() * Mth.TWO_PI;
        this.oRoll = this.roll;

        Entity owner = level.getEntity(entityId);
        if (owner != null) {
            this.yRot = owner.getYRot();
            this.xRot = owner.getXRot();
        }
    }

    private void refresh(final int mode) {
        if (mode == ShieldParticleOptions.COLLAPSE) {
            this.startCollapse();
            return;
        }
        this.lastHeartbeat = this.age;
        if (mode == ShieldParticleOptions.IMPACT) {
            this.pulse = 1.0F;
        }
    }

    private void startCollapse() {
        if (this.collapseTicks < 0) {
            this.collapseTicks = 0;
        }
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.oRoll = this.roll;
        this.oPulse = this.pulse;

        this.age++;
        this.roll += SPIN_PER_TICK;
        this.pulse *= 0.78F;

        Entity owner = this.level.getEntity(this.entityId);
        if (owner == null || !owner.isAlive()) {
            this.startCollapse();
        } else {
            this.yRot = owner.getYRot();
            this.xRot = owner.getXRot();
            Vec3 center = ShieldGeometry.center(owner, 1.0F);
            this.setPos(center.x, center.y, center.z);
        }

        if (this.collapseTicks >= 0) {
            if (++this.collapseTicks >= COLLAPSE_TICKS) {
                this.remove();
            }
            return;
        }
        if (this.age - this.lastHeartbeat > HEARTBEAT_TIMEOUT) {
            this.startCollapse();
            return;
        }

        if (owner != null && this.age % 2 == 0) {
            this.spawnSpark(owner);
        }
    }

    /** A little drifting glint along the rim, so the barrier feels alive. */
    private void spawnSpark(final Entity owner) {
        Vec3 normal = ShieldGeometry.normal(owner.getYRot(), owner.getXRot());
        Vec3 right = normal.cross(new Vec3(0.0D, 1.0D, 0.0D)).normalize();
        Vec3 up = right.cross(normal).normalize();

        double angle = this.random.nextDouble() * Math.PI * 2.0D;
        double radius = ShieldGeometry.RADIUS * (0.75D + this.random.nextDouble() * 0.25D);
        Vec3 point = new Vec3(this.x, this.y, this.z)
            .add(right.scale(Math.cos(angle) * radius))
            .add(up.scale(Math.sin(angle) * radius));
        this.level.addParticle(ParticleTypes.END_ROD, point.x, point.y, point.z,
            0.0D, 0.005D + this.random.nextDouble() * 0.01D, 0.0D);
    }

    @Override
    public void extract(final QuadParticleRenderState state, final Camera camera, final float partialTick) {
        Vec3 center;
        float yaw = this.yRot;
        float pitch = this.xRot;
        Entity owner = this.level.getEntity(this.entityId);
        if (owner != null && owner.isAlive()) {
            yaw = owner.getViewYRot(partialTick);
            pitch = owner.getViewXRot(partialTick);
            center = ShieldGeometry.center(owner, partialTick);
        } else {
            center = new Vec3(
                Mth.lerp(partialTick, this.xo, this.x),
                Mth.lerp(partialTick, this.yo, this.y),
                Mth.lerp(partialTick, this.zo, this.z));
        }

        float appear = Mth.clamp((this.age + partialTick) / APPEAR_TICKS, 0.0F, 1.0F);
        float fade = 1.0F;
        float shrink = 1.0F;
        if (this.collapseTicks >= 0) {
            float t = Mth.clamp((this.collapseTicks + partialTick) / COLLAPSE_TICKS, 0.0F, 1.0F);
            fade = 1.0F - t;
            shrink = 1.0F - 0.35F * t;
        }

        float size = this.quadSize * (0.6F + 0.4F * appear) * shrink;
        float pulse = Mth.lerp(partialTick, this.oPulse, this.pulse);
        float alpha = Mth.clamp((OUTER_ALPHA + 0.3F * pulse) * appear * fade, 0.0F, 1.0F);
        float glow = 1.0F + 0.35F * pulse;

        Quaternionf outward = ShieldGeometry.orientation(yaw, pitch);
        outward.rotateZ(Mth.lerp(partialTick, this.oRoll, this.roll));
        Quaternionf inward = new Quaternionf(outward).rotateY(Mth.PI);

        boolean firstPersonSelf = owner != null
            && owner == Minecraft.getInstance().player
            && Minecraft.getInstance().options.getCameraType().isFirstPerson();
        float innerAlpha = (firstPersonSelf ? INNER_ALPHA_FIRST_PERSON : INNER_ALPHA) * appear * fade;

        Vec3 cam = camera.position();
        float x = (float) (center.x - cam.x);
        float y = (float) (center.y - cam.y);
        float z = (float) (center.z - cam.z);

        this.add(state, outward, x, y, z, size, alpha, glow, partialTick);
        this.add(state, inward, x, y, z, size, innerAlpha, glow, partialTick);
    }

    private void add(
        final QuadParticleRenderState state,
        final Quaternionf rotation,
        final float x,
        final float y,
        final float z,
        final float size,
        final float alpha,
        final float glow,
        final float partialTick
    ) {
        if (alpha <= 0.003F) {
            return;
        }
        state.add(
            this.getLayer(),
            x, y, z,
            rotation.x, rotation.y, rotation.z, rotation.w,
            size,
            this.getU0(), this.getU1(), this.getV0(), this.getV1(),
            ARGB.colorFromFloat(alpha, Math.min(1.0F, this.rCol * glow), Math.min(1.0F, this.gCol * glow), Math.min(1.0F, this.bCol * glow)),
            this.getLightCoords(partialTick)
        );
    }

    @Override
    public void remove() {
        ACTIVE.remove(this.entityId, this);
        super.remove();
    }

    @Override
    protected SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    @Override
    protected int getLightCoords(final float partialTick) {
        return LightCoordsUtil.FULL_BRIGHT;
    }

    public static class Provider implements ParticleProvider<ShieldParticleOptions> {
        private final SpriteSet sprites;

        public Provider(final SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public @Nullable Particle createParticle(
            final ShieldParticleOptions options,
            final ClientLevel level,
            final double x,
            final double y,
            final double z,
            final double xAux,
            final double yAux,
            final double zAux,
            final RandomSource random
        ) {
            ShieldParticle existing = ACTIVE.get(options.entityId());
            if (existing != null && existing.isAlive() && existing.level == level) {
                existing.refresh(options.mode());
                return null;
            }
            if (options.mode() == ShieldParticleOptions.COLLAPSE) {
                return null;
            }
            ShieldParticle particle = new ShieldParticle(level, x, y, z, options.entityId(), this.sprites);
            particle.refresh(options.mode());
            ACTIVE.put(options.entityId(), particle);
            return particle;
        }
    }
}
