package com.aura.lamplight.folio.effect;

import com.aura.lamplight.folio.Folio;
import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

// save effect applier
@SuppressWarnings ("all")
final class EffectSources {
    private EffectSources() {}

    private static final AttachmentType<Map<String, String>> APPLIERS =
        AttachmentRegistry.<Map<String, String>>builder()
            .persistent(Codec.unboundedMap(Codec.STRING, Codec.STRING))
            .buildAndRegister(Folio.id("effect_appliers"));

    static void initialize() {
    }

    static void remember(final LivingEntity entity, final SchoolStatus status, final @Nullable LivingEntity applier) {
        Map<String, String> existing = entity.getAttached(APPLIERS);
        Map<String, String> updated = existing == null ? new HashMap<>() : new HashMap<>(existing);
        if (applier == null || applier == entity) {
            updated.remove(status.name());
        } else {
            updated.put(status.name(), applier.getUUID().toString());
        }
        entity.setAttached(APPLIERS, Map.copyOf(updated));
    }

    static @Nullable LivingEntity applier(final ServerLevel level, final LivingEntity entity, final SchoolStatus status) {
        Map<String, String> appliers = entity.getAttached(APPLIERS);
        if (appliers == null) {
            return null;
        }
        String raw = appliers.get(status.name());
        if (raw == null) {
            return null;
        }
        UUID id;
        try {
            id = UUID.fromString(raw);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(id);
        if (player != null) {
            return player.level() == level ? player : null;
        }
        Entity found = level.getEntity(id);
        return found instanceof LivingEntity living ? living : null;
    }
}
