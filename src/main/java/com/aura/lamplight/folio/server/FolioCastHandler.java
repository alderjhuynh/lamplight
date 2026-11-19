package com.aura.lamplight.folio.server;

import com.aura.lamplight.folio.CastSpellPayload;
import com.aura.lamplight.folio.CastingStatePayload;
import com.aura.lamplight.folio.Folio;
import com.aura.lamplight.folio.SelectSpellPayload;
import com.aura.lamplight.folio.SpellCooldownPayload;
import com.aura.lamplight.folio.item.SpellbookItem;
import com.aura.lamplight.folio.spell.Spell;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class FolioCastHandler {
    private FolioCastHandler() {}

    private static final int CHANNEL_INTERVAL = 10;
    private static final Identifier CAST_SLOW_ID = Folio.id("casting_slow");

    private static final Map<UUID, Casting> CASTING = new HashMap<>();
    private static final Map<UUID, Map<Identifier, Long>> COOLDOWNS = new HashMap<>();

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(FolioCastHandler::tick);
        ServerLivingEntityEvents.AFTER_DAMAGE.register(FolioCastHandler::afterDamage);
        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.DISCONNECT.register(
            (handler, server) -> {
                UUID id = handler.player.getUUID();
                CASTING.remove(id);
                COOLDOWNS.remove(id);
            });

        ServerPlayNetworking.registerGlobalReceiver(CastSpellPayload.TYPE, (payload, context) ->
            context.server().execute(() -> castSelected(context.player(), payload.spellIndex())));

        ServerPlayNetworking.registerGlobalReceiver(SelectSpellPayload.TYPE, (payload, context) ->
            context.server().execute(() -> select(context.player(), payload.spellIndex())));
    }

    public static void select(final ServerPlayer player, final int index) {
        SpellbookItem.Equipped equipped = SpellbookItem.findEquipped(player);
        if (equipped == null) {
            return;
        }
        int size = SpellbookItem.getSpells(equipped.stack()).size();
        if (size == 0) {
            return;
        }
        SpellbookItem.setSelected(equipped.stack(), index);
        equipped.sync(player);
    }

    public static void castSelected(final ServerPlayer player, final int requestedIndex) {
        if (!player.isAlive() || player.isRemoved()) {
            return;
        }

        SpellbookItem.Equipped equipped = SpellbookItem.findEquipped(player);
        if (equipped == null) {
            message(player, "message.lamplight.spellbook.missing", ChatFormatting.RED);
            return;
        }

        ItemStack book = equipped.stack();
        int size = SpellbookItem.getSpells(book).size();
        if (size == 0) {
            message(player, "message.lamplight.spellbook.empty", ChatFormatting.GRAY);
            return;
        }

        int index = requestedIndex < 0 ? SpellbookItem.getSelected(book) : Math.floorMod(requestedIndex, size);
        Spell spell = SpellbookItem.spellAt(book, index);
        if (spell == null) {
            return;
        }

        UUID uuid = player.getUUID();
        if (CASTING.containsKey(uuid)) {
            message(player, "message.lamplight.spellbook.busy", ChatFormatting.GRAY);
            return;
        }

        long now = player.level().getGameTime();
        long remaining = remainingCooldown(uuid, spell.id(), now);
        if (remaining > 0) {
            message(player, Component.translatable("message.lamplight.spellbook.cooldown",
                Math.max(1, (remaining + 19) / 20)), ChatFormatting.GRAY);
            return;
        }

        if (!FolioManaHandler.hasMana(player, spell.manaCost())) {
            player.level().playSound(null, player.blockPosition(), SoundEvents.VILLAGER_NO,
                SoundSource.PLAYERS, 0.7F, 1.0F);
            message(player, "message.lamplight.spellbook.no_mana", ChatFormatting.RED);
            return;
        }

        FolioManaHandler.spend(player, spell.manaCost());
        SpellbookItem.setSelected(book, index);
        equipped.sync(player);

        ItemStack source = SpellbookItem.scrollFor(spell);
        if (source.isEmpty()) {
            source = player.getMainHandItem();
        }

        int total = Math.max(0, spell.castTime());
        if (total <= 0) {
            complete(player, spell, source, now);
            return;
        }

        CASTING.put(uuid, new Casting(spell, source, now, now + total, total, now + CHANNEL_INTERVAL));
        applySlow(player);
        sendCastingState(player, spell, total, total, spell.channeled());
    }

    private static void tick(final MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID uuid = player.getUUID();
            Casting casting = CASTING.get(uuid);
            if (casting == null) {
                continue;
            }

            if (!player.isAlive() || player.isRemoved()) {
                cancel(player, false);
                continue;
            }

            long now = player.level().getGameTime();
            if (casting.spell.channeled() && now >= casting.nextChannelTick && now < casting.finishTick) {
                casting.spell.castTick(player.level(), player, casting.source);
                casting.nextChannelTick = now + CHANNEL_INTERVAL;
                sendCastingState(player, casting.spell, casting.totalTicks,
                    (int) Math.max(0, casting.finishTick - now), true);
            }

            if (now >= casting.finishTick) {
                complete(player, casting.spell, casting.source, now);
            }
        }
    }

    private static void complete(
        final ServerPlayer player,
        final Spell spell,
        final ItemStack source,
        final long now
    ) {
        CASTING.remove(player.getUUID());
        clearSlow(player);

        spell.cast(player.level(), player, source);

        if (spell.cooldownTicks() > 0) {
            COOLDOWNS.computeIfAbsent(player.getUUID(), ignored -> new HashMap<>())
                .put(spell.id(), now + spell.cooldownTicks());
            ServerPlayNetworking.send(player,
                new SpellCooldownPayload(spell.id().toString(), spell.cooldownTicks()));
        }
        sendCastingState(player, null, 0, 0, false);
    }

    public static void cancel(final ServerPlayer player, final boolean interrupted) {
        Casting casting = CASTING.remove(player.getUUID());
        if (casting == null) {
            return;
        }
        clearSlow(player);
        sendCastingState(player, null, 0, 0, false);
        if (interrupted) {
            message(player, "message.lamplight.spellbook.interrupted", ChatFormatting.RED);
        }
    }

    private static void afterDamage(
        final LivingEntity entity,
        final net.minecraft.world.damagesource.DamageSource source,
        final float baseDamage,
        final float damageTaken,
        final boolean blocked
    ) {
        if (!(entity instanceof ServerPlayer player) || damageTaken <= 0.0F) {
            return;
        }
        Casting casting = CASTING.get(player.getUUID());
        if (casting != null && casting.spell.interruptible()) {
            cancel(player, true);
        }
    }

    public static boolean isCasting(final ServerPlayer player) {
        return CASTING.containsKey(player.getUUID());
    }

    public record CastView(Spell spell, float progress) {}

    public static @Nullable CastView castView(final ServerPlayer player) {
        Casting casting = CASTING.get(player.getUUID());
        if (casting == null) {
            return null;
        }
        long remaining = Math.max(0L, casting.finishTick - player.level().getGameTime());
        float progress = casting.totalTicks <= 0
            ? 1.0F
            : 1.0F - Math.min(1.0F, (float) remaining / (float) casting.totalTicks);
        return new CastView(casting.spell, progress);
    }

    private static long remainingCooldown(final UUID uuid, final Identifier spellId, final long now) {
        Map<Identifier, Long> cooldowns = COOLDOWNS.get(uuid);
        if (cooldowns == null) {
            return 0L;
        }
        Long end = cooldowns.get(spellId);
        if (end == null) {
            return 0L;
        }
        long remaining = end - now;
        if (remaining <= 0L) {
            cooldowns.remove(spellId);
            return 0L;
        }
        return remaining;
    }

    private static void applySlow(final ServerPlayer player) {
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }
        speed.removeModifier(CAST_SLOW_ID);
        speed.addTransientModifier(new AttributeModifier(
            CAST_SLOW_ID, -0.35D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    private static void clearSlow(final ServerPlayer player) {
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.removeModifier(CAST_SLOW_ID);
        }
    }

    private static void sendCastingState(
        final ServerPlayer player,
        final @Nullable Spell spell,
        final int total,
        final int remaining,
        final boolean channeled
    ) {
        ServerPlayNetworking.send(player, new CastingStatePayload(
            spell == null ? "" : spell.id().toString(), total, remaining, channeled));
    }

    private static void message(final ServerPlayer player, final String key, final ChatFormatting color) {
        message(player, Component.translatable(key), color);
    }

    private static void message(final ServerPlayer player, final Component text, final ChatFormatting color) {
        player.sendSystemMessage(text.copy().withStyle(color), true);
    }

    private static final class Casting {
        final Spell spell;
        final ItemStack source;
        final long startTick;
        final long finishTick;
        final int totalTicks;
        long nextChannelTick;

        Casting(
            final Spell spell,
            final ItemStack source,
            final long startTick,
            final long finishTick,
            final int totalTicks,
            final long nextChannelTick
        ) {
            this.spell = spell;
            this.source = source;
            this.startTick = startTick;
            this.finishTick = finishTick;
            this.totalTicks = totalTicks;
            this.nextChannelTick = nextChannelTick;
        }
    }
}
