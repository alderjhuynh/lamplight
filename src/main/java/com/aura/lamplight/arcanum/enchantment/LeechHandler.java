package com.aura.lamplight.arcanum.enchantment;

import com.aura.lamplight.mixin.arcanum.ThrownTridentLoyaltyAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class LeechHandler {
    private LeechHandler() {}

    static final int LATCH_TICKS_BASE = 100;
    static final int LATCH_TICKS_PER_LEVEL = 40;

    static final int DOT_INTERVAL_TICKS = 10;
    static final float DOT_DAMAGE = 1.0F;

    private static final class Latch {
        final int victimId;
        final UUID ownerId;
        int ticksLeft;

        Latch(int victimId, UUID ownerId, int ticksLeft) {
            this.victimId = victimId;
            this.ownerId = ownerId;
            this.ticksLeft = ticksLeft;
        }
    }

    private static final Map<UUID, Latch> LATCHES = new ConcurrentHashMap<>();

    public static void startLatch(ThrownTrident trident, LivingEntity victim, int level) {
        if (level <= 0 || !victim.isAlive()) return;
        Entity owner = trident.getOwner();
        if (owner == null || victim == owner) return;
        if (!(trident.level() instanceof ServerLevel)) return;
        int ticks = LATCH_TICKS_BASE + Math.max(0, level - 1) * LATCH_TICKS_PER_LEVEL;
        LATCHES.put(trident.getUUID(), new Latch(victim.getId(), owner.getUUID(), ticks));
        setHoming(trident, false);
        trident.noPhysics = true;
    }

    public static void tickLatch(ThrownTrident trident) {
        if (!(trident.level() instanceof ServerLevel server)) return;
        Latch latch = LATCHES.get(trident.getUUID());
        if (latch == null) return;

        LivingEntity victim = findVictim(server, latch);
        Player owner = server.getServer().getPlayerList().getPlayer(latch.ownerId);
        if (victim == null || victim.isRemoved() || !victim.isAlive()
                || owner == null || owner.isRemoved() || !owner.isAlive()) {
            releaseLatch(trident);
            return;
        }

        // TODO: leech attaching to targets looks really fucking ugly
        trident.noPhysics = true;
        trident.setDeltaMovement(Vec3.ZERO);
        trident.setPos(victim.getX(), victim.getEyeY() - victim.getBbHeight() * 0.35D, victim.getZ());

        if (latch.ticksLeft % DOT_INTERVAL_TICKS == 0) {
            DamageSource source = server.damageSources().indirectMagic(trident, owner);
            if (victim.hurtServer(server, source, DOT_DAMAGE) && victim.isAlive()) {
                owner.heal(DOT_DAMAGE);
            }
        }

        if (--latch.ticksLeft <= 0) {
            releaseLatch(trident);
        }
    }

    public static boolean isLatched(Entity trident) {
        return LATCHES.containsKey(trident.getUUID());
    }

    private static void releaseLatch(ThrownTrident trident) {
        LATCHES.remove(trident.getUUID());
        setHoming(trident, true);
    }

    private static void setHoming(ThrownTrident trident, boolean enabled) {
        trident.getEntityData().set(ThrownTridentLoyaltyAccessor.lamplight$loyaltyData(), enabled ? (byte) 1 : (byte) 0);
    }

    private static LivingEntity findVictim(ServerLevel server, Latch latch) {
        Entity entity = server.getEntity(latch.victimId);
        return entity instanceof LivingEntity living ? living : null;
    }
}
