package com.aura.lamplight.folio.server;

import com.aura.lamplight.folio.Folio;
import com.aura.lamplight.folio.particle.ModParticles;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

// trust this is a peak spell i just havent finished it yet
public final class WindChainHandler {

    private WindChainHandler() {}

    private static final Map<UUID, Long> BUFFED = new HashMap<>();
    private static final Map<UUID, Chain> CHAINS = new HashMap<>();

    private static final double THRESHOLD = 5.0D;
    private static final int LOCK_TICKS = 40; 
    private static final int CHAIN_DURATION_TICKS = 120;
    private static final double CHAIN_CHANCE = 0.4D;

    private static final Identifier LOCK_MODIFIER = Folio.id("wind_chain_lock");

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(WindChainHandler::tick);
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            UUID uuid = handler.getPlayer().getUUID();
            BUFFED.remove(uuid);
            CHAINS.entrySet().removeIf(e -> e.getValue().caster.equals(uuid) || e.getKey().equals(uuid));
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClientSide()) return InteractionResult.PASS;
            if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
            if (!(entity instanceof LivingEntity target)) return InteractionResult.PASS;
            if (player.getUUID().equals(target.getUUID())) return InteractionResult.PASS;
            if (target instanceof Player p && (p.isCreative() || p.isSpectator())) return InteractionResult.PASS;
            if (!isBuffed(player)) return InteractionResult.PASS;
            if (CHAINS.containsKey(target.getUUID())) return InteractionResult.PASS;
            if (world.getRandom().nextDouble() >= CHAIN_CHANCE) return InteractionResult.PASS;

            if (player instanceof ServerPlayer serverPlayer) {
                chain(serverPlayer, target);
            }
            return InteractionResult.PASS;
        });
    }

    public static void buff(ServerPlayer caster, int durationTicks) {
        long now = caster.level().getGameTime();
        BUFFED.put(caster.getUUID(), now + durationTicks);
    }

    public static boolean isBuffed(Player player) {
        Long expiry = BUFFED.get(player.getUUID());
        if (expiry == null) return false;
        if (player.level().getGameTime() >= expiry) {
            BUFFED.remove(player.getUUID());
            return false;
        }
        return true;
    }

    private static void chain(ServerPlayer caster, LivingEntity target) {
        long now = caster.level().getGameTime();
        CHAINS.put(target.getUUID(), new Chain(caster.getUUID(), now + CHAIN_DURATION_TICKS, caster.level().dimension()));
        caster.level().playSound(
            null, target.getX(), target.getY(), target.getZ(),
            SoundEvents.CHAIN_PLACE, SoundSource.PLAYERS, 0.8F, 1.2F
        );
        emitLine(caster.level(), target, caster);
    }

    private static void tick(MinecraftServer server) {
        if (CHAINS.isEmpty()) return;

        Iterator<Map.Entry<UUID, Chain>> it = CHAINS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Chain> entry = it.next();
            UUID targetId = entry.getKey();
            Chain chain = entry.getValue();

            ServerLevel level = server.getLevel(chain.dimension);
            if (level == null) { it.remove(); continue; }

            Entity targetEntity = level.getEntity(targetId);
            if (!(targetEntity instanceof LivingEntity target) || !target.isAlive()) { it.remove(); continue; }

            ServerPlayer caster = server.getPlayerList().getPlayer(chain.caster);
            if (caster == null || caster.level().dimension() != chain.dimension) { it.remove(); continue; }

            long now = level.getGameTime();

            if (!chain.locked) {
                if (now >= chain.expiry) { it.remove(); continue; }
                double dist = target.position().distanceTo(caster.position());
                if (dist > THRESHOLD) {
                    chain.locked = true;
                    chain.lockExpiry = now + LOCK_TICKS;
                    chain.lockedX = target.getX();
                    chain.lockedY = target.getY();
                    chain.lockedZ = target.getZ();
                    chain.lockedYRot = target.getYRot();
                    chain.lockedXRot = target.getXRot();
                    lock(target, chain);
                    caster.level().playSound(
                        null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 0.6F, 1.4F
                    );
                    spawnRing(caster);
                }
            } else {
                if (now >= chain.lockExpiry) {
                    unlock(target);
                    it.remove();
                    continue;
                }
                lock(target, chain);
            }

            emitLine(level, target, caster);
        }
    }

    private static void lock(LivingEntity target, Chain chain) {
        target.setDeltaMovement(0.0D, 0.0D, 0.0D);
        target.setNoGravity(true);
        AttributeInstance move = target.getAttribute(Attributes.MOVEMENT_SPEED);
        if (move != null) {
            if (!move.hasModifier(LOCK_MODIFIER)) {
                move.addOrUpdateTransientModifier(new AttributeModifier(LOCK_MODIFIER, -1.0D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        }
        if (target instanceof ServerPlayer p) {
            p.hurtMarked = true;
            p.absSnapTo(chain.lockedX, chain.lockedY, chain.lockedZ, chain.lockedYRot, chain.lockedXRot);
        } else {
            target.setPos(chain.lockedX, chain.lockedY, chain.lockedZ);
            if (target instanceof net.minecraft.world.entity.Mob mob) mob.getNavigation().stop();
        }
    }

    private static void unlock(LivingEntity target) {
        target.setNoGravity(false);
        AttributeInstance move = target.getAttribute(Attributes.MOVEMENT_SPEED);
        if (move != null && move.hasModifier(LOCK_MODIFIER)) {
            move.removeModifier(LOCK_MODIFIER);
        }
        if (target instanceof ServerPlayer p) {
            p.hurtMarked = true;
        }
    }

    private static void spawnRing(ServerPlayer caster) {
        double cx = caster.getX();
        double cy = caster.getY() + 0.1D;
        double cz = caster.getZ();
        int points = 16;
        double r = 1.4D;
        for (int i = 0; i < points; i++) {
            double ang = (2.0D * Math.PI * i) / points;
            double x = cx + Math.cos(ang) * r;
            double z = cz + Math.sin(ang) * r;
            caster.level().sendParticles(ModParticles.CHAIN_LINK, x, cy, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private static void emitLine(ServerLevel level, LivingEntity target, ServerPlayer caster) {
        Vec3 t = target.getBoundingBox().getCenter();
        Vec3 c = caster.getBoundingBox().getCenter();
        int steps = 10;
        for (int i = 0; i <= steps; i++) {
            double f = i / (double) steps;
            double x = t.x + (c.x - t.x) * f;
            double y = t.y + (c.y - t.y) * f;
            double z = t.z + (c.z - t.z) * f;
            level.sendParticles(ModParticles.CHAIN_LINK, x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private static final class Chain {
        final UUID caster;
        final long expiry;
        final ResourceKey<Level> dimension;
        boolean locked;
        long lockExpiry;
        double lockedX, lockedY, lockedZ;
        float lockedYRot, lockedXRot;

        Chain(UUID caster, long expiry, ResourceKey<Level> dimension) {
            this.caster = caster;
            this.expiry = expiry;
            this.dimension = dimension;
        }
    }
}
