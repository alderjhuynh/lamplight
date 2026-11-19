package com.aura.lamplight.folio.server;

import com.aura.lamplight.folio.DragonbladeStatePayload;
import com.aura.lamplight.folio.DragonbladeTeleportRequestPayload;
import com.aura.lamplight.folio.temporal.TemporalField;
import com.aura.lamplight.folio.temporal.TemporalFields;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class DragonbladeHandler {

    private DragonbladeHandler() {}

    private static final Map<UUID, State> STATES = new HashMap<>();
    private static final Map<UUID, Boolean> FORCED_CRIT = new HashMap<>();

    private static final int GRACE_TICKS = 200;
    private static final int RESET_TICKS = 40;
    private static final double TELEPORT_RANGE = 30.0D;

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(DragonbladeHandler::tick);

        ServerPlayNetworking.registerGlobalReceiver(DragonbladeTeleportRequestPayload.TYPE, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayer sp = context.player();
                State state = STATES.get(sp.getUUID());
                if (state == null) return;
                Entity target = sp.level().getEntity(payload.entityId());
                if (!(target instanceof LivingEntity living) || !living.isAlive()) return;
                if (target == sp) return;
                if (sp.position().distanceTo(target.position()) > TELEPORT_RANGE) return;
                if (state.teleportedEntities.contains(target.getUUID())) return;

                ServerLevel level = sp.level();
                Vec3 tp = target.position();
                sp.teleportTo(level, tp.x, tp.y, tp.z, java.util.Set.of(), sp.getYRot(), sp.getXRot(), false);
                state.teleportedEntities.add(target.getUUID());
                state.expiry = level.getGameTime() + RESET_TICKS;
                state.durationTicks = RESET_TICKS;
                state.hasTeleported = true;
                state.bossBar.setColor(BossEvent.BossBarColor.RED);
                requestForcedCrit(sp);
                sp.attack(living);

                level.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8F, 1.2F);
            });
        });
    }

    public static void activate(ServerPlayer caster) {
        long now = caster.level().getGameTime();
        deactivate(caster.getUUID(), caster.level().getServer());

        TemporalField field = TemporalField.builder(caster.level())
            .follow(caster)
            .radius(0.5D)
            .tickRate(10.0F)
            .duration(GRACE_TICKS)
            .filter(e -> e instanceof Player p && p.getUUID().equals(caster.getUUID()))
            .build();
        TemporalFields.add(field);

        ServerBossEvent bossBar = new ServerBossEvent(
            java.util.UUID.randomUUID(),
            Component.literal("Dragonblade"),
            BossEvent.BossBarColor.PURPLE,
            BossEvent.BossBarOverlay.PROGRESS
        );
        bossBar.setProgress(1.0F);
        bossBar.addPlayer(caster);

        State state = new State(caster.getUUID(), now + GRACE_TICKS, caster.level().dimension(), field, bossBar, now, GRACE_TICKS);
        STATES.put(caster.getUUID(), state);

        ServerPlayNetworking.send(caster, new DragonbladeStatePayload(true, GRACE_TICKS));

        caster.level().playSound(
            null, caster.getX(), caster.getY(), caster.getZ(),
            SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.0F, 0.6F
        );
    }

    private static void tick(MinecraftServer server) {
        if (STATES.isEmpty()) return;
        Iterator<Map.Entry<UUID, State>> it = STATES.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, State> e = it.next();
            State state = e.getValue();
            ServerPlayer player = server.getPlayerList().getPlayer(state.caster);
            if (player == null || player.level().dimension() != state.dimension) {
                deactivate(e.getKey(), server);
                it.remove();
                continue;
            }
            ServerLevel level = player.level();
            long now = level.getGameTime();
            if (now >= state.expiry) {
                deactivate(e.getKey(), server);
                it.remove();
                continue;
            }
            float progress = (state.expiry - now) / (float) state.durationTicks;
            state.bossBar.setProgress(Math.max(0.0F, Math.min(1.0F, progress)));
            state.bossBar.setName(Component.literal("Dragonblade"));
        }
    }

    private static void deactivate(UUID caster, MinecraftServer server) {
        State state = STATES.get(caster);
        if (state == null) return;
        TemporalFields.remove(state.field);
        state.bossBar.removeAllPlayers();
        ServerPlayer player = (server != null) ? server.getPlayerList().getPlayer(caster) : null;
        if (player != null) {
            ServerPlayNetworking.send(player, new DragonbladeStatePayload(false, 0));
        }
    }

    public static boolean isForcedCrit(Player player) {
        return FORCED_CRIT.getOrDefault(player.getUUID(), Boolean.FALSE);
    }

    public static void clearForcedCrit(Player player) {
        FORCED_CRIT.remove(player.getUUID());
    }

    private static void requestForcedCrit(ServerPlayer player) {
        FORCED_CRIT.put(player.getUUID(), Boolean.TRUE);
    }

    private static final class State {
        final UUID caster;
        long expiry;
        final ResourceKey<Level> dimension;
        final TemporalField field;
        final ServerBossEvent bossBar;
        int durationTicks;
        boolean hasTeleported = false;
        final Set<UUID> teleportedEntities = new HashSet<>();

        State(UUID caster, long expiry, ResourceKey<Level> dimension, TemporalField field, ServerBossEvent bossBar, long now, int durationTicks) {
            this.caster = caster;
            this.expiry = expiry;
            this.dimension = dimension;
            this.field = field;
            this.bossBar = bossBar;
            this.durationTicks = durationTicks;
        }
    }
}
