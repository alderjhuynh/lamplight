package com.aura.lamplight.arcanum.server;

import com.aura.lamplight.arcanum.enchantment.ArcanumEnchantmentHelper;
import com.aura.lamplight.arcanum.network.ArcanumPackets;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class FlowGlideHandler {
    private static final double ACCELERATION = 0.04D;
    private static final double VERTICAL_BOOST = 0.05D;

    private static final java.util.Set<UUID> GLIDING = ConcurrentHashMap.newKeySet();

    private FlowGlideHandler() {}

    public static boolean isGliding(ServerPlayer player) {
        return GLIDING.contains(player.getUUID());
    }

    public static void tick(MinecraftServer server) {
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            UUID id = p.getUUID();
            if (p.onGround() || p.isFallFlying() || p.isInWater() || p.isInLava()) {
                GLIDING.remove(id);
            }
        }
    }

    public static void clear(UUID id) {
        GLIDING.remove(id);
    }

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(ArcanumPackets.FlowGlidePayload.TYPE, (payload, context) ->
                context.server().execute(() -> handleGlide(context.player())));
    }

    private static void handleGlide(ServerPlayer player) {
        if (!ArcanumEnchantmentHelper.hasFlowGlidePrereqs(player)) return;
        if (player.onGround() || player.isFallFlying()) return;
        if (player.isInWater() || player.isInLava()) return;

        GLIDING.add(player.getUUID());

        Vec3 velocity = player.getDeltaMovement();
        double x = velocity.x;
        double z = velocity.z;
        double horizLengthSq = x * x + z * z;
        Vec3 direction;
        if (horizLengthSq < 1.0E-6D) return;
        double scale = 1.0 / Math.sqrt(horizLengthSq);
        direction = new Vec3(x * scale, 0, z * scale);

        player.addDeltaMovement(new Vec3(
                direction.x * ACCELERATION,
                VERTICAL_BOOST,
                direction.z * ACCELERATION
        ));
        player.hurtMarked = true;
    }
}
