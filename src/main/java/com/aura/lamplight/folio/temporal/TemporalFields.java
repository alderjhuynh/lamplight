package com.aura.lamplight.folio.temporal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.protocol.game.ClientboundTickingStatePacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Central registry and driver for {@link TemporalField}s.
 *
 * <p>It is responsible for:</p>
 * <ul>
 *   <li>expiring and cleaning up fields each server tick;</li>
 *   <li>deciding, for any entity, whether the current server tick should
 *       actually run its tick (the throttling that simulates a different
 *       tick rate); and</li>
 *   <li>keeping affected players' clients in sync by sending
 *       {@link ClientboundTickingStatePacket}s, so that input, movement
 *       and rendering also slow down exactly the way a real
 *       {@code /tick rate} change would.</li>
 * </ul>
 *
 * <p>Call {@link #initialize()} once during mod init. Fields are activated
 * with {@link #add(TemporalField)}.</p>
 */
public final class TemporalFields {

    private TemporalFields() {}

    private static final List<TemporalField> FIELDS = new ArrayList<>();
    private static final Map<Entity, TickState> STATES = new WeakHashMap<>();
    private static final Map<ServerPlayer, SentTickState> SENT = new WeakHashMap<>();

    private static float lastGlobalRate = -1.0F;
    private static boolean lastGlobalFrozen = false;

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(TemporalFields::tick);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            FIELDS.clear();
            STATES.clear();
            SENT.clear();
        });
    }

    /** Activates a field. */
    public static void add(TemporalField field) {
        FIELDS.add(field);
    }

    /** Deactivates a field. */
    public static void remove(TemporalField field) {
        FIELDS.remove(field);
    }

    /** Removes every active field. */
    public static void clear() {
        FIELDS.clear();
    }

    /**
     * Returns {@code true} if the entity should be ticked on this server
     * tick, {@code false} if it should be skipped to simulate a slower
     * tick rate. Safe to call from mixins.
     */
    public static boolean shouldTick(Entity entity) {
        float multiplier = multiplierFor(entity);
        if (multiplier >= 1.0F) {
            STATES.remove(entity);
            return true;
        }
        MinecraftServer server = entity.level().getServer();
        if (server == null) {
            return true;
        }
        int tick = server.getTickCount();
        TickState state = STATES.computeIfAbsent(entity, e -> new TickState());
        if (state.tickCount == tick) {
            return state.tick;
        }
        state.tickCount = tick;
        state.accumulator += multiplier;
        if (state.accumulator >= 1.0F) {
            state.accumulator -= 1.0F;
            state.tick = true;
        } else {
            state.tick = false;
        }
        return state.tick;
    }

    /**
     * The fraction of a normal tick this entity currently lives through,
     * based on the strongest (slowest) field affecting it. {@code 1.0} means
     * "every tick", {@code 0.5} means "every other tick".
     */
    public static float multiplierFor(Entity entity) {
        MinecraftServer server = entity.level().getServer();
        if (server == null) {
            return 1.0F;
        }
        float global = Math.max(1.0F, server.tickRateManager().tickrate());
        float bestRate = Float.MAX_VALUE;
        for (TemporalField field : FIELDS) {
            if (field.tickRate() < bestRate && field.contains(entity)) {
                bestRate = field.tickRate();
            }
        }
        if (bestRate == Float.MAX_VALUE || bestRate >= global) {
            return 1.0F;
        }
        return bestRate / global;
    }

    private static void tick(MinecraftServer server) {
        if (!FIELDS.isEmpty()) {
            Iterator<TemporalField> it = FIELDS.iterator();
            while (it.hasNext()) {
                TemporalField field = it.next();
                ServerLevel level = server.getLevel(field.dimension());
                if (level == null || field.isAnchorInvalid(level) || field.isExpired(level)) {
                    it.remove();
                    continue;
                }
                emitParticles(level, field, server.getTickCount());
            }
        }
        syncClientTickRates(server);
    }

    private static void emitParticles(ServerLevel level, TemporalField field, int serverTick) {
        ParticleOptions particle = field.particle();
        if (particle == null) {
            return;
        }
        if (serverTick % field.particleIntervalTicks() != 0) {
            return;
        }
        Vec3 c = field.center();
        int count = field.particleCount();
        double r = field.radius();
        level.sendParticles(particle, c.x, c.y + 0.1D, c.z, count, r * 0.75D, 0.3D, r * 0.75D, 0.01D);
    }

    private static void syncClientTickRates(MinecraftServer server) {
        float global = Math.max(1.0F, server.tickRateManager().tickrate());
        boolean frozen = server.tickRateManager().isFrozen();
        if (global != lastGlobalRate || frozen != lastGlobalFrozen) {
            SENT.clear();
            lastGlobalRate = global;
            lastGlobalFrozen = frozen;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            float desired = desiredClientRate(player, global);
            ResourceKey<Level> dim = player.level().dimension();
            SentTickState sent = SENT.get(player);
            if (sent == null || sent.dimension != dim || sent.rate != desired || sent.frozen != frozen) {
                player.connection.send(new ClientboundTickingStatePacket(desired, frozen));
                SENT.put(player, new SentTickState(dim, desired, frozen));
            }
        }
    }

    private static float desiredClientRate(ServerPlayer player, float global) {
        float best = Float.MAX_VALUE;
        for (TemporalField field : FIELDS) {
            if (field.tickRate() < best && field.contains(player)) {
                best = field.tickRate();
            }
        }
        if (best == Float.MAX_VALUE || best >= global) {
            return global;
        }
        return best;
    }

    private static final class TickState {
        int tickCount = -1;
        float accumulator = 0.0F;
        boolean tick = true;
    }

    private record SentTickState(ResourceKey<Level> dimension, float rate, boolean frozen) {}
}
