package com.aura.lamplight.folio.client;

import com.aura.lamplight.folio.CastingStatePayload;
import com.aura.lamplight.folio.CastSpellPayload;
import com.aura.lamplight.folio.ReleaseSpellPayload;
import com.aura.lamplight.folio.SelectSpellPayload;
import com.aura.lamplight.folio.SpellCooldownPayload;
import com.aura.lamplight.folio.item.SpellbookItem;
import com.aura.lamplight.folio.spell.ChargeSpell;
import com.aura.lamplight.folio.spell.Spell;
import com.aura.lamplight.folio.spell.Spells;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SpellbookClientState {
    private SpellbookClientState() {}

    private static List<Identifier> spells = List.of();
    private static int selected = -1;
    private static boolean equipped = false;

    private static boolean casting = false;
    private static String castingSpell = "";
    private static int castTotal = 0;
    private static int castRemaining = 0;
    private static boolean channeled = false;

    private static boolean holdingCast = false;

    private static final Map<Identifier, Integer> COOLDOWNS = new HashMap<>();
    private static int lastSentSelection = Integer.MIN_VALUE;
   private static int pendingSelection = Integer.MIN_VALUE;

    public static void refresh(final Minecraft client) {
        equipped = client.player != null && SpellbookItem.findEquipped(client.player) != null;
        if (!equipped) {
            spells = List.of();
            selected = -1;
            pendingSelection = Integer.MIN_VALUE;
            return;
        }

        SpellbookItem.Equipped book = SpellbookItem.findEquipped(client.player);
        spells = List.copyOf(SpellbookItem.getSpells(book.stack()));

        int bookSelected = SpellbookItem.getSelected(book.stack());
        if (pendingSelection != Integer.MIN_VALUE) {
            if (bookSelected == pendingSelection || pendingSelection >= spells.size()) {
                pendingSelection = Integer.MIN_VALUE;
                selected = bookSelected;
            } else if (selected < 0 || selected >= spells.size()) {
                selected = bookSelected;
            }
        } else {
            selected = bookSelected;
        }
    }

    public static List<Identifier> spells() {
        return spells;
    }

    public static boolean hasSpellbook() {
        return equipped;
    }

    public static int selected() {
        return spells.isEmpty() ? -1 : Math.max(0, Math.min(selected, spells.size() - 1));
    }

    public static @Nullable Spell selectedSpell() {
        int index = selected();
        return index < 0 || index >= spells.size() ? null : Spells.byId(spells.get(index));
    }

    public static void select(final int index) {
        if (spells.isEmpty() || index < 0 || index >= spells.size()) {
            return;
        }
        selected = index;
        if (index != lastSentSelection) {
            lastSentSelection = index;
            pendingSelection = index;
            ClientPlayNetworking.send(new SelectSpellPayload(index));
        }
    }

    /**
     * Casts the selected spell as a tap: charge spells are started and released in the same tick, so
     * they fire at minimum power. Used by the spell wheel, where there is no key to hold.
     */
    public static void castSelected() {
        castSelectedHeld();
        releaseCast();
    }

    /**
     * Starts casting the selected spell from the cast key. If it is a charge spell, the caller must
     * call {@link #releaseCast()} once the key is let go (FolioClient does this every tick).
     */
    public static void castSelectedHeld() {
        if (spells.isEmpty()) {
            return;
        }
        int index = selected();
        ClientPlayNetworking.send(new CastSpellPayload(index < 0 ? -1 : index));
        holdingCast = selectedSpell() instanceof ChargeSpell;
    }

    public static boolean isHoldingCast() {
        return holdingCast;
    }

    public static void releaseCast() {
        if (!holdingCast) {
            return;
        }
        holdingCast = false;
        ClientPlayNetworking.send(ReleaseSpellPayload.INSTANCE);
    }

    public static boolean isCasting() {
        return casting;
    }

    public static String castingSpell() {
        return castingSpell;
    }

    public static int castTotal() {
        return castTotal;
    }

    public static int castRemaining() {
        return castRemaining;
    }

    public static boolean channeled() {
        return channeled;
    }

    public static float castProgress() {
        if (!casting || castTotal <= 0) {
            return 0.0F;
        }
        return 1.0F - (float) castRemaining / (float) castTotal;
    }

    public static int cooldown(final Identifier spellId) {
        return COOLDOWNS.getOrDefault(spellId, 0);
    }

    public static int cooldownForSelected() {
        Spell spell = selectedSpell();
        return spell == null ? 0 : cooldown(spell.id());
    }

    public static void applyCasting(final CastingStatePayload payload) {
        if (payload.spell().isEmpty()) {
            casting = false;
            castingSpell = "";
            castTotal = 0;
            castRemaining = 0;
            channeled = false;
            return;
        }
        casting = true;
        castingSpell = payload.spell();
        castTotal = payload.totalTicks();
        castRemaining = Math.min(payload.totalTicks(), payload.remainingTicks());
        channeled = payload.channeled();
    }

    public static void applyCooldown(final SpellCooldownPayload payload) {
        Identifier id = Identifier.tryParse(payload.spell());
        if (id == null) {
            return;
        }
        if (payload.remainingTicks() <= 0) {
            COOLDOWNS.remove(id);
        } else {
            COOLDOWNS.put(id, payload.remainingTicks());
        }
    }

    public static void tick() {
        if (casting && castRemaining > 0) {
            castRemaining--;
            if (castRemaining <= 0) {
                casting = false;
            }
        }
        if (COOLDOWNS.isEmpty()) {
            return;
        }
        List<Identifier> expired = new ArrayList<>();
        for (Map.Entry<Identifier, Integer> entry : COOLDOWNS.entrySet()) {
            int next = entry.getValue() - 1;
            if (next <= 0) {
                expired.add(entry.getKey());
            } else {
                entry.setValue(next);
            }
        }
        for (Identifier id : expired) {
            COOLDOWNS.remove(id);
        }
    }

    public static void reset() {
        spells = List.of();
        selected = -1;
        equipped = false;
        casting = false;
        castingSpell = "";
        castTotal = 0;
        castRemaining = 0;
        channeled = false;
        holdingCast = false;
        COOLDOWNS.clear();
        lastSentSelection = Integer.MIN_VALUE;
        pendingSelection = Integer.MIN_VALUE;
    }
}
