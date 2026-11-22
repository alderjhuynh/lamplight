package com.aura.lamplight.folio.spell;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import com.aura.lamplight.folio.Folio;

public enum SpellSchool {
    ICE("ice", "Ice"),
    LIGHTNING("lightning", "Lightning"),
    NATURE("nature", "Nature"),
    ARCANE("arcane", "Arcane"),
    FLAME("flame", "Flame"),
    BLOOD("blood", "Blood"),
    WIND("wind", "Wind"),
    HOLY("holy", "Holy");

    private final String id;
    private final String displayName;

    SpellSchool(final String id, final String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String id() {
        return id;
    }

    public Component displayName() {
        return Component.translatable("school.lamplight." + id);
    }

    public Identifier scrollItemId() {
        return Folio.id(id + "_scroll");
    }

    @Override
    public String toString() {
        return displayName;
    }
}
