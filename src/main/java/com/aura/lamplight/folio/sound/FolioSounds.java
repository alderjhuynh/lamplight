package com.aura.lamplight.folio.sound;

import com.aura.lamplight.folio.Folio;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

public final class FolioSounds {
    private FolioSounds() {}

    public static final SoundEvent GRAVITY_WELL = register("spell.gravity_well");

    public static final SoundEvent  FIREBALL_PREPARE = register("spell.fireball.prepare");
    public static final SoundEvent FIREBALL_INITIAL_CAST = register("spell.fireball.initial_cast");
    public static final SoundEvent FIREBALL_CAST = register("spell.fireball.cast");
    public static final SoundEvent FIREBALL_IMPACT = register("spell.fireball.impact");
    public static final SoundEvent FIREBALL_IMPACT_HEAVY = register("spell.fireball.impact_heavy");

    private static SoundEvent register(final String path) {
        return Registry.register(BuiltInRegistries.SOUND_EVENT, Folio.id(path),
            SoundEvent.createVariableRangeEvent(Folio.id(path)));
    }

    public static void initialize() {
        Folio.LOGGER.info("Registered folio sounds");
    }
}
