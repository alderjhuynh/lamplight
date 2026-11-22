package com.aura.lamplight.folio.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
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

public class HolyBoltEntity extends ThrowableItemProjectile {

    private static final float DAMAGE = 4.0F;
    private static final float UNDEAD_BONUS = 4.0F;

    public HolyBoltEntity(final EntityType<? extends HolyBoltEntity> type, final Level level) {
        super(type, level);
    }

    public HolyBoltEntity(final Level level, final LivingEntity owner, final ItemStack itemStack) {
        super(ModEntityTypes.HOLY_BOLT, owner, level, itemStack);
    }

    public HolyBoltEntity(final Level level, final double x, final double y, final double z, final ItemStack itemStack) {
        super(ModEntityTypes.HOLY_BOLT, x, y, z, level, itemStack);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.GLOWSTONE_DUST;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.01D;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.level().addParticle(
                ParticleTypes.END_ROD,
                this.getX(), this.getY(), this.getZ(),
                0.0D, 0.0D, 0.0D
            );
        }
    }

    @Override
    @SuppressWarnings ("deprecation")
    protected void onHitEntity(final EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        if (this.level() instanceof ServerLevel level) {
            Entity target = hitResult.getEntity();
            float damage = DAMAGE;
            if (target.getType().builtInRegistryHolder().is(EntityTypeTags.UNDEAD)) {
                damage += UNDEAD_BONUS;
            }
            target.hurtServer(level, this.damageSources().indirectMagic(this, this.getOwner()), damage);
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
            for (int i = 0; i < 12; i++) {
                this.level().addParticle(
                    ParticleTypes.END_ROD,
                    this.getX(), this.getY(), this.getZ(),
                    (this.random.nextDouble() - 0.5D) * 0.15D,
                    (this.random.nextDouble() - 0.5D) * 0.15D,
                    (this.random.nextDouble() - 0.5D) * 0.15D
                );
            }
        } else {
            super.handleEntityEvent(id);
        }
    }
}
