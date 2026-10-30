package com.aura.lamplight.arcanum.enchantment;

import com.aura.lamplight.arcanum.Arcanum;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class ReelingHandler {
    private ReelingHandler() {}

    static final float DAMAGE_BASE = 4.0F;
    static final float DAMAGE_PER_LEVEL = 1.0F;

    static final float DRAG_PER_LEVEL = 0.35F;
    static final float DRAG_BASE = 0.55F;

    static final int COOLDOWN_TICKS_BASE = 120;
    static final int COOLDOWN_TICKS_PER_LEVEL = 40;

    public static final ResourceKey<DamageType> REELING = ResourceKey.create(
            Registries.DAMAGE_TYPE, Arcanum.id("reeling"));

    public static float damageFor(int level) {
        return DAMAGE_BASE + Math.max(0, level - 1) * DAMAGE_PER_LEVEL;
    }

    static float dragFor(int level) {
        return DRAG_BASE + Math.max(0, level - 1) * DRAG_PER_LEVEL;
    }

    static int cooldownFor(int level) {
        return COOLDOWN_TICKS_BASE + Math.max(0, level - 1) * COOLDOWN_TICKS_PER_LEVEL;
    }

    public static boolean landReelingHit(Entity trident, LivingEntity victim, Entity owner, int level) {
        if (!(trident.level() instanceof ServerLevel server) || level <= 0) {
            return false;
        }
        DamageSource source = createSource(server, trident, owner);
        float expected = damageFor(level);
        float healthBefore = victim.getHealth();
        boolean landed = victim.hurtServer(server, source, expected);
        if (landed) {
            float dealt = healthBefore - victim.getHealth();
            if (dealt > 0) {
                drag(victim, owner, dragFor(level));
            }
            if (owner instanceof Player player) {
                player.getCooldowns().addCooldown(tridentStack(trident), cooldownFor(level));
            }
        }
        return landed;
    }

    private static void drag(LivingEntity victim, Entity owner, float force) {
        Vec3 dir = owner.position().add(0.0D, owner.getBbHeight() * 0.5D, 0.0D)
                .subtract(victim.position().add(0.0D, victim.getBbHeight() * 0.5D, 0.0D));
        if (dir.lengthSqr() < 1.0E-4D) {
            return;
        }
        victim.setDeltaMovement(victim.getDeltaMovement().add(dir.normalize().scale(force)));
        victim.hurtMarked = true;
    }

    private static DamageSource createSource(ServerLevel level, Entity trident, Entity owner) {
        Holder.Reference<DamageType> holder = level.registryAccess()
                .lookupOrThrow(Registries.DAMAGE_TYPE)
                .getOrThrow(REELING);
        return new DamageSource(holder, trident, owner);
    }

    static ItemStack tridentStack(Entity trident) {
        return trident instanceof AbstractArrow arrow ? arrow.getPickupItemStackOrigin() : ItemStack.EMPTY;
    }
}
