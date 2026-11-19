package com.aura.lamplight.folio.server;

import com.aura.lamplight.folio.attribute.FolioAttributes;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class FolioManaHandler {
    private FolioManaHandler() {}

    private static final Holder<Attribute> MANA = BuiltInRegistries.ATTRIBUTE.wrapAsHolder(FolioAttributes.MANA);
    private static final Holder<Attribute> MAX_MANA = BuiltInRegistries.ATTRIBUTE.wrapAsHolder(FolioAttributes.MAX_MANA);

    private static final double OUT_OF_COMBAT_REGEN = 0.20D;
    private static final double IN_COMBAT_REGEN = 0.06D;
    private static final long COMBAT_MEMORY_TICKS = 100L;

    private static final Map<UUID, Long> LAST_COMBAT = new HashMap<>();
    private static final Map<UUID, Double> REGEN_ACCUMULATOR = new HashMap<>();

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(FolioManaHandler::tick);
        ServerLivingEntityEvents.AFTER_DAMAGE.register(FolioManaHandler::afterDamage);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> refill(handler.player));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            UUID id = handler.player.getUUID();
            LAST_COMBAT.remove(id);
            REGEN_ACCUMULATOR.remove(id);
        });
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> refill(newPlayer));
    }

    public static double mana(final Player player) {
        AttributeInstance instance = player.getAttribute(MANA);
        return instance == null ? 0.0D : instance.getValue();
    }

    public static double maxMana(final Player player) {
        AttributeInstance instance = player.getAttribute(MAX_MANA);
        return instance == null ? 0.0D : Math.max(0.0D, instance.getValue());
    }

    public static boolean hasMana(final Player player, final double amount) {
        return mana(player) + 1.0E-6D >= amount;
    }

    public static boolean spend(final Player player, final double amount) {
        AttributeInstance instance = player.getAttribute(MANA);
        if (instance == null || instance.getValue() + 1.0E-6D < amount) {
            return false;
        }
        instance.setBaseValue(Math.max(0.0D, instance.getValue() - amount));
        return true;
    }

    public static void refill(final Player player) {
        AttributeInstance instance = player.getAttribute(MANA);
        AttributeInstance max = player.getAttribute(MAX_MANA);
        if (instance != null && max != null) {
            instance.setBaseValue(Math.max(0.0D, max.getValue()));
            REGEN_ACCUMULATOR.remove(player.getUUID());
        }
    }

    public static void markCombat(final Player player) {
        LAST_COMBAT.put(player.getUUID(), player.level().getGameTime());
    }

    private static void afterDamage(
        final LivingEntity entity,
        final net.minecraft.world.damagesource.DamageSource source,
        final float baseDamage,
        final float damageTaken,
        final boolean blocked
    ) {
        if (entity instanceof ServerPlayer hurt && damageTaken > 0.0F) {
            markCombat(hurt);
        }
        if (source.getEntity() instanceof ServerPlayer attacker) {
            markCombat(attacker);
        }
    }

    private static void tick(final MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            AttributeInstance mana = player.getAttribute(MANA);
            AttributeInstance max = player.getAttribute(MAX_MANA);
            if (mana == null || max == null) {
                continue;
            }

            double maxValue = Math.max(0.0D, max.getValue());
            if (mana.getValue() > maxValue) {
                mana.setBaseValue(maxValue);
            }

            long now = player.level().getGameTime();
            Long last = LAST_COMBAT.get(player.getUUID());
            boolean inCombat = last != null && now - last < COMBAT_MEMORY_TICKS;
            double rate = inCombat ? IN_COMBAT_REGEN : OUT_OF_COMBAT_REGEN;
            if (mana.getValue() < maxValue) {
                UUID id = player.getUUID();
                double accumulated = REGEN_ACCUMULATOR.getOrDefault(id, 0.0D) + rate;
                if (accumulated >= 1.0D) {
                    double whole = Math.floor(accumulated);
                    mana.setBaseValue(Math.min(maxValue, mana.getValue() + whole));
                    accumulated -= whole;
                }
                REGEN_ACCUMULATOR.put(id, accumulated);
            } else {
                REGEN_ACCUMULATOR.remove(player.getUUID());
            }
        }
    }

    public static @Nullable AttributeInstance instance(final Player player, final Holder<Attribute> attribute) {
        return player.getAttribute(attribute);
    }
}
