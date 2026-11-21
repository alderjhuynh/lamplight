package com.aura.lamplight.folio.server;

import com.aura.lamplight.folio.particle.ModParticles;
import com.aura.lamplight.folio.sound.FolioSounds;
import com.aura.lamplight.folio.temporal.TemporalField;
import com.aura.lamplight.folio.temporal.TemporalFields;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

public final class GravityWellHandler {

    private GravityWellHandler() {}

    private static final Map<UUID, Well> WELLS = new HashMap<>();

    private static final double[] RADII = { 3.0D, 5.0D, 7.5D };
    private static final float[] TICK_RATES = { 16.0F, 10.0F, 2.0F };
    private static final int[] STAGE_DURATIONS = { 20, 20, 60 };
    private static final float[] SOUND_PITCHES = { 1.0F, 0.85F, 0.7F };
    private static final float[] SOUND_VOLUMES = { 1.0F, 1.15F, 1.3F };

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(GravityWellHandler::tick);
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
            deactivate(handler.getPlayer().getUUID()));
    }

    public static void activate(final ServerLevel level, final ServerPlayer caster) {
        deactivate(caster.getUUID());

        long now = level.getGameTime();
        int totalDuration = 0;
        for (int duration : STAGE_DURATIONS) {
            totalDuration += duration;
        }

        TemporalField field = TemporalField.builder(level)
            .follow(caster)
            .radius(RADII[0])
            .tickRate(TICK_RATES[0])
            .duration(totalDuration)
            .filter(entity -> !entity.getUUID().equals(caster.getUUID()))
            .build();
        TemporalFields.add(field);

        WELLS.put(caster.getUUID(), new Well(
            field,
            level.dimension(),
            now + STAGE_DURATIONS[0],
            now + totalDuration
        ));

        playStage(level, caster, 0);
    }

    private static void tick(final MinecraftServer server) {
        if (WELLS.isEmpty()) {
            return;
        }

        Iterator<Map.Entry<UUID, Well>> it = WELLS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Well> entry = it.next();
            Well well = entry.getValue();
            ServerPlayer caster = server.getPlayerList().getPlayer(entry.getKey());

            if (caster == null || !caster.isAlive() || caster.level().dimension() != well.dimension) {
                TemporalFields.remove(well.field);
                it.remove();
                continue;
            }

            ServerLevel level = caster.level();
            long now = level.getGameTime();
            if (now >= well.expiry) {
                TemporalFields.remove(well.field);
                it.remove();
                continue;
            }

            while (well.stage < RADII.length - 1 && now >= well.stageEndsAt) {
                well.stage++;
                well.stageEndsAt += STAGE_DURATIONS[well.stage];
                well.field.setRadius(RADII[well.stage]);
                well.field.setTickRate(TICK_RATES[well.stage]);
                playStage(level, caster, well.stage);
            }
        }
    }

    private static void playStage(final ServerLevel level, final ServerPlayer caster, final int stage) {
        level.playSound(
            null,
            caster.getX(),
            caster.getY(),
            caster.getZ(),
            FolioSounds.GRAVITY_WELL,
            SoundSource.PLAYERS,
            SOUND_VOLUMES[stage],
            SOUND_PITCHES[stage]
        );

        double radius = RADII[stage];
        int points = Math.max(16, (int) Math.round(radius * 12.0D));
        double centerY = caster.getY() + 0.1D;
        for (int i = 0; i < points; i++) {
            double angle = (2.0D * Math.PI * i) / points;
            double x = caster.getX() + Math.cos(angle) * radius;
            double z = caster.getZ() + Math.sin(angle) * radius;
            level.sendParticles(ModParticles.GRAVITY_WELL, x, centerY, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private static void deactivate(final UUID caster) {
        Well well = WELLS.remove(caster);
        if (well != null) {
            TemporalFields.remove(well.field);
        }
    }

    private static final class Well {
        final TemporalField field;
        final ResourceKey<Level> dimension;
        final long expiry;
        long stageEndsAt;
        int stage;

        Well(final TemporalField field, final ResourceKey<Level> dimension, final long stageEndsAt, final long expiry) {
            this.field = field;
            this.dimension = dimension;
            this.stageEndsAt = stageEndsAt;
            this.expiry = expiry;
        }
    }
}
