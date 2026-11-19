package com.aura.lamplight.folio.spell;

import com.aura.lamplight.folio.Folio;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public final class Spells {
    private Spells() {}

    private static final Map<Identifier, Spell> BY_ID = new LinkedHashMap<>();

    // arcane
    public static final Spell DRAGONBLADE = register(new DragonbladeSpell());

    //i ice
    public static final Spell FROST_BOLT = register(new FrostBoltSpell());
    public static final Spell SLOWING_AURA = register(new SlowingAuraSpell());

    // wind
    public static final Spell GALE_TETHER = register(new GaleTetherSpell());

    // uhhh whatever
    public static final Spell GRAVITY_WELL = register(new GravityWellSpell());

    private static Spell register(final Spell spell) {
        Spell previous = BY_ID.putIfAbsent(spell.id(), spell);
        if (previous != null) {
            throw new IllegalStateException("Duplicate spell id: " + spell.id());
        }
        return spell;
    }

    public static @Nullable Spell byId(final Identifier id) {
        return BY_ID.get(id);
    }

    public static void initialize() {
        Folio.LOGGER.info("Registered {} spells for {}", BY_ID.size(), Folio.MOD_ID);
    }
}
