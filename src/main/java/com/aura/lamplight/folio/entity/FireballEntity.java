package com.aura.lamplight.folio.entity;

import com.aura.lamplight.folio.particle.BlastwaveParticleOptions;
import com.aura.lamplight.folio.particle.FlameChargeParticleOptions;
import com.aura.lamplight.folio.particle.ModParticles;
import com.aura.lamplight.folio.sound.FolioSounds;
import com.aura.lamplight.folio.spell.FireballSpell;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ExplosionParticleInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.random.WeightedList;

public class FireballEntity extends ThrowableProjectile {

    public static final int MAX_CHARGE_TICKS = 60;

    public static final float MIN_VISUAL_SIZE = 0.30F;
    public static final float MAX_VISUAL_SIZE = 1.25F;

    private static final float MIN_EXPLOSION_POWER = 1.0F;
    private static final float MAX_EXPLOSION_POWER = 4.5F;

    private static final float MIN_DIRECT_DAMAGE = 4.0F;
    private static final float MAX_DIRECT_DAMAGE = 10.0F;

    private static final float MIN_CHARGE_SPEED = 1.5F;
    private static final float MAX_CHARGE_SPEED = 0.9F;

    private static final float MIN_IGNITE_SECONDS = 3.0F;
    private static final float MAX_IGNITE_SECONDS = 8.0F;

    private static final int HEAVY_IMPACT_CHARGE_TICKS = MAX_CHARGE_TICKS / 2;

    private static final int MAX_FLIGHT_TICKS = 100;

    private static final double AIM_RANGE = 64.0D;

    private static final boolean SELF_DAMAGE = false;
    private static final float SELF_KNOCKBACK = 0.3F;

    private static final double ORB_BASE_DISTANCE = 1.4D;
    private static final double ORB_DISTANCE_PER_SIZE = 1.1D;
    private static final double ORB_MIN_DISTANCE = 0.45D;
    private static final double ORB_FOLLOW_RATE = 0.35D;

    private static final double ORB_MAX_FOLLOW_STEP = 1.25D;

   private static final Holder<SoundEvent> SILENT_EXPLOSION = Holder.direct(
        SoundEvent.createVariableRangeEvent(Identifier.withDefaultNamespace("intentionally_empty")));

    private static final WeightedList<ExplosionParticleInfo> BLOCK_PARTICLES = WeightedList.<ExplosionParticleInfo>builder()
        .add(new ExplosionParticleInfo(ParticleTypes.POOF, 0.5F, 1.0F))
        .add(new ExplosionParticleInfo(ParticleTypes.SMOKE, 1.0F, 1.0F))
        .build();

    private static final EntityDataAccessor<Float> DATA_CHARGE =
        SynchedEntityData.defineId(FireballEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_LAUNCHED =
        SynchedEntityData.defineId(FireballEntity.class, EntityDataSerializers.BOOLEAN);

    private int chargeTicks;
    private boolean starved;
    private int flightTicks;
    private int visualTimer;


    public FireballEntity(final EntityType<? extends FireballEntity> type, final Level level) {
        super(type, level);
    }

    public FireballEntity(final Level level, final LivingEntity owner) {
        this(ModEntityTypes.FIREBALL, level);
        this.setOwner(owner);
        Vec3 center = orbCenter(owner, visualSize(0.0F), 1.0F);
        this.setPos(center.x, center.y - this.getBbHeight() / 2.0F, center.z);
    }

    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder entityData) {
        entityData.define(DATA_CHARGE, 0.0F);
        entityData.define(DATA_LAUNCHED, false);
    }

    public float charge() {
        return this.entityData.get(DATA_CHARGE);
    }

    public boolean isLaunched() {
        return this.entityData.get(DATA_LAUNCHED);
    }

    public int chargeTicks() {
        return this.chargeTicks;
    }

    public boolean isFullyCharged() {
        return this.chargeTicks >= MAX_CHARGE_TICKS;
    }

    public boolean isStarved() {
        return this.starved;
    }

    public void setStarved(final boolean starved) {
        this.starved = starved;
    }

    public void addChargeTick() {
        if (this.chargeTicks < MAX_CHARGE_TICKS) {
            this.chargeTicks++;
            this.entityData.set(DATA_CHARGE, this.chargeTicks / (float) MAX_CHARGE_TICKS);
        }
    }

    public static float visualSize(final float charge) {
        return Mth.lerp(charge, MIN_VISUAL_SIZE, MAX_VISUAL_SIZE);
    }

    public static Vec3 orbCenter(final Entity owner, final float visualSize, final float partialTick) {
        Vec3 eye = owner.getEyePosition(partialTick);
        Vec3 look = owner.getViewVector(partialTick);

        double distance = ORB_BASE_DISTANCE + visualSize * ORB_DISTANCE_PER_SIZE;
        Vec3 wanted = eye.add(look.scale(distance));

        Vec3 delta = wanted.subtract(eye);
        double length = delta.length();
        BlockHitResult hit = owner.level().clip(
            new ClipContext(eye, wanted, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner));
        if (hit.getType() != HitResult.Type.MISS && length > 1.0E-4D) {
            double allowed = Math.max(ORB_MIN_DISTANCE, hit.getLocation().distanceTo(eye) - visualSize * 0.5D);
            if (allowed < length) {
                return eye.add(delta.scale(allowed / length));
            }
        }
        return wanted;
    }

    @Override
    public void tick() {
        if (this.isLaunched()) {
            this.tickFlight();
        } else {
            this.tickCharging();
        }
    }

    private void tickCharging() {
        this.baseTick();

        if (this.level() instanceof ServerLevel) {
            Entity owner = this.getOwner();
            if (owner == null
                || !owner.isAlive()
                || owner.level() != this.level()
                || !FireballSpell.isOrbOf(owner, this)) {
                this.discard();
                return;
            }
            Vec3 halfHeight = new Vec3(0.0D, this.getBbHeight() / 2.0D, 0.0D);
            Vec3 current = this.position().add(halfHeight);
            Vec3 target = orbCenter(owner, visualSize(this.charge()), 1.0F);
            Vec3 next = current.add(target.subtract(current).scale(ORB_FOLLOW_RATE));

            Vec3 step = next.subtract(current);
            if (step.lengthSqr() > ORB_MAX_FOLLOW_STEP * ORB_MAX_FOLLOW_STEP) {
                next = current.add(step.normalize().scale(ORB_MAX_FOLLOW_STEP));
            }

            this.setPos(next.x, next.y - halfHeight.y, next.z);
            this.setYRot(owner.getYRot());
            this.setXRot(owner.getXRot());
        } else {
            this.keepVisualAlive();
        }
    }

    private void tickFlight() {
        if (this.level() instanceof ServerLevel) {
            if (++this.flightTicks > MAX_FLIGHT_TICKS || this.isInWater()) {
                this.fizzle();
                return;
            }
        } else {
            this.keepVisualAlive();
        }
        super.tick();
    }

    private void keepVisualAlive() {
        if (this.visualTimer-- <= 0) {
            this.visualTimer = 10;
            this.level().addParticle(
                new FlameChargeParticleOptions(this.getId()),
                this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
        }
    }

    public void launch(final Entity owner) {
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }

        Vec3 center = this.position().add(0.0D, this.getBbHeight() / 2.0D, 0.0D);
        Vec3 eye = owner.getEyePosition();
        Vec3 look = owner.getViewVector(1.0F);

        BlockHitResult hit = level.clip(new ClipContext(
            eye, eye.add(look.scale(AIM_RANGE)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner));
        Vec3 direction = hit.getLocation().subtract(center);
        if (direction.lengthSqr() < 0.25D) {
            direction = look;
        }

        this.entityData.set(DATA_LAUNCHED, true);
        this.shoot(direction.x, direction.y, direction.z, Mth.lerp(this.charge(), MIN_CHARGE_SPEED, MAX_CHARGE_SPEED), 0.0F);

        this.spawnLaunchEffects(level, center);
    }
    private void spawnLaunchEffects(final ServerLevel level, final Vec3 center) {
        float charge = this.charge();
        double spread = 0.18D + charge * 0.20D;
        double speed = 0.12D + charge * 0.18D;

        level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 1.0F, 0.72F, 0.32F),
            center.x, center.y, center.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(ParticleTypes.FLAME, center.x, center.y, center.z,
            24 + (int) (charge * 40.0F), spread, spread, spread, speed);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, center.x, center.y, center.z,
            8 + (int) (charge * 10.0F), spread * 1.2D, spread * 1.2D, spread * 1.2D, 0.02D);
        level.sendParticles(ParticleTypes.LAVA, center.x, center.y, center.z,
            6 + (int) (charge * 10.0F), spread, spread, spread, 0.0D);
        level.sendParticles(new BlastwaveParticleOptions(0.7F + charge * 0.9F),
            center.x, center.y, center.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);

        level.playSound(null, center.x, center.y, center.z, FolioSounds.FIREBALL_CAST, SoundSource.PLAYERS,
            0.8F + charge * 0.5F, 1.05F - charge * 0.15F);
    }

    @Override
    protected void onHitEntity(final EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        if (this.level() instanceof ServerLevel level) {
            Entity target = hitResult.getEntity();
            float charge = this.charge();
            target.hurtServer(level, this.damageSources().indirectMagic(this, this.getOwner()),
                Mth.lerp(charge, MIN_DIRECT_DAMAGE, MAX_DIRECT_DAMAGE));
            target.igniteForSeconds(Mth.lerp(charge, MIN_IGNITE_SECONDS, MAX_IGNITE_SECONDS));
        }
    }

    @Override
    protected void onHit(final HitResult hitResult) {
        super.onHit(hitResult);
        if (this.level() instanceof ServerLevel level && this.isAlive()) {
            this.detonate(level);
        }
    }

    private void detonate(final ServerLevel level) {
        float charge = this.charge();
        float power = Mth.lerp(charge, MIN_EXPLOSION_POWER, MAX_EXPLOSION_POWER);
        Entity owner = this.getOwner();
        Vec3 center = this.getBoundingBox().getCenter();

        boolean grief = level.getGameRules().get(GameRules.MOB_GRIEFING);
        level.explode(
            this,
            this.damageSources().explosion(this, owner),
            new OwnerAwareCalculator(owner),
            center.x, center.y, center.z,
            power,
            grief,
            Level.ExplosionInteraction.MOB,
            ParticleTypes.EXPLOSION, 
            ParticleTypes.EXPLOSION_EMITTER, 
            BLOCK_PARTICLES,
            SILENT_EXPLOSION);

        double spread = 0.25D + power * 0.3D;
        int flames = 30 + (int) (power * 22.0F);

        level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 1.0F, 0.65F, 0.25F),
            center.x, center.y, center.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(ParticleTypes.FLAME, center.x, center.y, center.z,
            flames, spread, spread, spread, 0.12D);
        level.sendParticles(ParticleTypes.LAVA, center.x, center.y, center.z,
            8 + (int) (power * 5.0F), spread, spread, spread, 0.0D);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, center.x, center.y, center.z,
            10 + (int) (power * 6.0F), spread * 0.9D, spread * 0.6D, spread * 0.9D, 0.035D);
        level.sendParticles(ParticleTypes.SMOKE, center.x, center.y, center.z,
            12 + (int) (power * 8.0F), spread * 1.3D, spread * 0.8D, spread * 1.3D, 0.02D);
        level.sendParticles(ModParticles.FIERY_SMOKE, center.x, center.y, center.z,
            10 + (int) (power * 6.0F), spread * 0.9D, spread * 0.6D, spread * 0.9D, 0.04D);
        level.sendParticles(new BlastwaveParticleOptions(1.4F + power * 0.75F),
            center.x, center.y, center.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);

        boolean heavy = this.chargeTicks >= HEAVY_IMPACT_CHARGE_TICKS;
       level.playSound(null, center.x, center.y, center.z,
            heavy ? FolioSounds.FIREBALL_IMPACT_HEAVY : FolioSounds.FIREBALL_IMPACT, SoundSource.BLOCKS,
            heavy ? 5.0F : 4.0F, 0.95F + this.random.nextFloat() * 0.1F);

        this.discard();
    }

    public void fizzle() {
        if (this.level() instanceof ServerLevel level) {
            Vec3 center = this.getBoundingBox().getCenter();
            level.sendParticles(ParticleTypes.SMOKE, center.x, center.y, center.z,
                8 + (int) (this.charge() * 16.0F), 0.3D, 0.3D, 0.3D, 0.02D);
            level.playSound(null, center.x, center.y, center.z, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS,
                0.6F, 1.0F);
        }
        this.discard();
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0D;
    }

    @Override
    protected float getAirDrag() {
        return 1.0F;
    }

    @Override
    public boolean canUsePortal(final boolean ignorePassenger) {
        return false;
    }

    private static final class OwnerAwareCalculator extends ExplosionDamageCalculator {
        private final @Nullable Entity owner;

        OwnerAwareCalculator(final @Nullable Entity owner) {
            this.owner = owner;
        }

        @Override
        public boolean shouldDamageEntity(final Explosion explosion, final Entity entity) {
            return SELF_DAMAGE || entity != this.owner;
        }

        @Override
        public float getKnockbackMultiplier(final Entity entity) {
            return entity == this.owner ? SELF_KNOCKBACK : 1.0F;
        }
    }
}
