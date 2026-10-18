package com.aura.lamplight.folio.spell;

import com.aura.lamplight.folio.Folio;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/**
 * Central lookup of every spell Folio knows about, keyed by {@link Identifier}.
 *
 * <p>This is intentionally a plain map rather than a full Minecraft
 * {@code Registry} for now - it's easy to swap for a datapack-driven
 * registry later without changing anything that calls {@link #byId}.</p>
 */
public final class Spells {
    private Spells() {}

    private static final Map<Identifier, Spell> BY_ID = new LinkedHashMap<>();

    // --- Ice scroll spells ---
    public static final Spell FROST_BOLT = register(new FrostBoltSpell());
    public static final Spell SLOWING_AURA = register(new SlowingAuraSpell());

    private static Spell register(final Spell spell) {
        Spell previous = BY_ID.putIfAbsent(spell.id(), spell);
        if (previous != null) {
            throw new IllegalStateException("Duplicate spell id: " + spell.id());
        }
        return spell;
    }

    /** Looks up a spell by id, or {@code null} if no such spell is registered. */
    public static @Nullable Spell byId(final Identifier id) {
        return BY_ID.get(id);
    }

    public static void initialize() {
        Folio.LOGGER.info("Registered {} spells for {}", BY_ID.size(), Folio.MOD_ID);
    }
}
