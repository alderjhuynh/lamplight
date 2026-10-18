package com.aura.lamplight.folio.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * The projectile spawned by the {@code lamplight:frost_bolt} spell.
 *
 * <p>Flies like a thrown snowball, deals a small amount of magic damage
 * on impact, and briefly slows whatever it hits. It currently renders as
 * a thrown {@link Items#ICE} via the vanilla {@code ThrownItemRenderer} -
 * swap {@link #getDefaultItem()} (and the client renderer registration in
 * {@code FolioClient}) once a bespoke projectile texture/model exists.</p>
 */
public class FrostBoltEntity extends ThrowableItemProjectile {

    private static final float DAMAGE = 3.0F;
    private static final int SLOW_DURATION_TICKS = 60; // 3 seconds
    private static final int SLOW_AMPLIFIER = 1; // Slowness II

    /** Used by the entity type factory when the entity is loaded from the world/network. */
    public FrostBoltEntity(final EntityType<? extends FrostBoltEntity> type, final Level level) {
        super(type, level);
    }

    /** Used by {@link net.minecraft.world.entity.projectile.Projectile#spawnProjectileFromRotation}. */
    public FrostBoltEntity(final Level level, final LivingEntity owner, final ItemStack itemStack) {
        super(ModEntityTypes.FROST_BOLT, owner, level, itemStack);
    }

    public FrostBoltEntity(final Level level, final double x, final double y, final double z, final ItemStack itemStack) {
        super(ModEntityTypes.FROST_BOLT, x, y, z, level, itemStack);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.ICE;
    }

    @Override
    protected double getDefaultGravity() {
        // Slightly lighter arc than a snowball so it reads as a "bolt".
        return 0.02;
    }

    @Override
    protected void onHitEntity(final EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        Entity target = hitResult.getEntity();
        if (this.level() instanceof ServerLevel serverLevel) {
            target.hurtServer(serverLevel, this.damageSources().indirectMagic(this, this.getOwner()), DAMAGE);
            if (target instanceof LivingEntity livingTarget) {
                livingTarget.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, SLOW_DURATION_TICKS, SLOW_AMPLIFIER));
            }
        }
    }

    @Override
    protected void onHit(final HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level().isClientSide()) {
            this.level().broadcastEntityEvent(this, (byte) 3);
            this.discard();
        }
    }

    @Override
    public void handleEntityEvent(final byte id) {
        if (id == 3) {
            for (int i = 0; i < 10; i++) {
                this.level().addParticle(ParticleTypes.SNOWFLAKE, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            }
        } else {
            super.handleEntityEvent(id);
        }
    }
}
