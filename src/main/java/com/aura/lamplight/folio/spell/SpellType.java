package com.aura.lamplight.folio.spell;

import net.minecraft.network.chat.Component;

public enum SpellType {
    NORMAL("normal"),
    CHARGE("charge"),
    CHANNELED("channeled"),
    MULTICAST("multicast");

    private final String id;

    SpellType(final String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public Component displayName() {
        return Component.translatable("spell_type.lamplight." + id);
    }

    @Override
    public String toString() {
        return id;
    }
}
