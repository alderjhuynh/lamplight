package com.aura.lamplight.puppeteer;

import com.aura.lamplight.puppeteer.animation.AnimationDefinition;
import com.aura.lamplight.puppeteer.animation.AnimationDefinitions;
import com.aura.lamplight.puppeteer.network.AnimationStatePayload;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

public final class PlayerAnimationTracker {
    private PlayerAnimationTracker() {}

    private record ActiveAnimation(Identifier animationId, long startGameTime, boolean oneShot, long oneShotExpiresAtTick) {}

    private static final ConcurrentHashMap<UUID, ActiveAnimation> ACTIVE = new ConcurrentHashMap<>();

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(PlayerAnimationTracker::onServerTick);
        ServerPlayConnectionEvents.DISCONNECT.register(
            (handler, server) -> ACTIVE.remove(handler.getPlayer().getUUID())
        );
    }

    private static void onServerTick(final MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            tickPlayer(player);
        }
    }

    private static void tickPlayer(final ServerPlayer player) {
        UUID uuid = player.getUUID();
        long gameTime = player.level().getGameTime();
        ActiveAnimation current = ACTIVE.get(uuid);

        if (current != null && current.oneShot() && gameTime < current.oneShotExpiresAtTick()) {
            return;
        }

        AnimationDefinition best = null;
        for (AnimationDefinition definition : AnimationDefinitions.all()) {
            if (definition.trigger().test(player) && (best == null || definition.priority() > best.priority())) {
                best = definition;
            }
        }

        Identifier newId = best == null ? null : best.id();
        Identifier currentId = current == null ? null : current.animationId();
        boolean oneShotJustExpired = current != null && current.oneShot();

        if (oneShotJustExpired || !Objects.equals(newId, currentId)) {
            setActive(player, newId, gameTime, false, -1L);
        }
    }

    public static void playOnce(final ServerPlayer player, final Identifier animationId) {
        AnimationDefinition definition = AnimationDefinitions.byId(animationId);
        if (definition == null) {
            Puppeteer.LOGGER.warn("playOnce() called with unknown animation id {}", animationId);
            return;
        }

        long gameTime = player.level().getGameTime();
        setActive(player, animationId, gameTime, true, gameTime + definition.lengthTicks());
    }

    private static void setActive(
        final ServerPlayer player,
        final @Nullable Identifier animationId,
        final long gameTime,
        final boolean oneShot,
        final long oneShotExpiresAtTick
    ) {
        UUID uuid = player.getUUID();
        if (animationId == null) {
            ACTIVE.remove(uuid);
        } else {
            ACTIVE.put(uuid, new ActiveAnimation(animationId, gameTime, oneShot, oneShotExpiresAtTick));
        }
        broadcast(player, animationId, gameTime);
    }

    private static void broadcast(final ServerPlayer player, final @Nullable Identifier animationId, final long startGameTime) {
        AnimationStatePayload payload = new AnimationStatePayload(player.getId(), Optional.ofNullable(animationId), startGameTime);
        ServerPlayNetworking.send(player, payload);
        for (ServerPlayer viewer : PlayerLookup.tracking(player)) {
            ServerPlayNetworking.send(viewer, payload);
        }
    }
}
