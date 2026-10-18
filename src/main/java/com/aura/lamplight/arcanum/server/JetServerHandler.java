package com.aura.lamplight.arcanum.server;

import com.aura.lamplight.arcanum.network.ArcanumPackets;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class JetServerHandler {
    private JetServerHandler() {}

    private static final double ACCELERATION = 0.09D;
    private static final double VERTICAL_BOOST = 0.05D;

    private static final Map<UUID, Integer> BOOST_TICKS = new ConcurrentHashMap<>();
    private static final java.util.Set<UUID> GLIDING = java.util.concurrent.ConcurrentHashMap.newKeySet();

    public static void setBoost(ServerPlayer player, int ticks) {
        BOOST_TICKS.put(player.getUUID(), ticks);

    }

    public static boolean isBoosted(ServerPlayer player) {
        return BOOST_TICKS.getOrDefault(player.getUUID(), 0) > 0;
    }

    public static boolean isGliding(ServerPlayer player) {
        return GLIDING.contains(player.getUUID());
    }

    public static int getBoostTicks(ServerPlayer player) {
        return BOOST_TICKS.getOrDefault(player.getUUID(), 0);
    }

    public static void tick(MinecraftServer server) {
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            UUID id = p.getUUID();
            BOOST_TICKS.computeIfPresent(id, (k, v) -> v > 0 ? v - 1 : 0);

            Integer v = BOOST_TICKS.get(id);
            if (v != null && v <= 0) BOOST_TICKS.remove(id);

            if (p.onGround() || p.isFallFlying() || p.isInWater() || p.isInLava()) {
                GLIDING.remove(id);

                if (p.onGround()) {
                    BOOST_TICKS.remove(id);
                }
            }
        }
    }

    public static void clear(UUID id) {
        BOOST_TICKS.remove(id);
        GLIDING.remove(id);
    }

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(ArcanumPackets.GlidePayload.TYPE, (payload, context) -> {
            context.server().execute(() -> handleGlide(context.player()));
        });
    }

    private static void handleGlide(ServerPlayer player) {

        if (player.onGround() || player.isFallFlying()) return;
        if (player.isInWater() || player.isInLava()) return;

        if (!isGliding(player)) {
            if (!isBoosted(player)) return;
            GLIDING.add(player.getUUID());
        }

        Vec3 velocity = player.getDeltaMovement();
        double x = velocity.x;
        double z = velocity.z;
        double horizLengthSq = x * x + z * z;
        Vec3 direction;
        if (horizLengthSq < 1.0E-6D) {

            Vec3 look = player.getLookAngle();
            direction = new Vec3(look.x, 0, look.z);
            if (direction.lengthSqr() < 1.0E-6D) return;
            direction = direction.normalize();
        } else {
            double scale = 1.0 / Math.sqrt(horizLengthSq);
            direction = new Vec3(x * scale, 0, z * scale);
        }

        player.addDeltaMovement(new Vec3(
                direction.x * ACCELERATION,
                VERTICAL_BOOST,
                direction.z * ACCELERATION
        ));
        player.hurtMarked = true;
    }
}
