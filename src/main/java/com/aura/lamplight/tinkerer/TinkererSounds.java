package com.aura.lamplight.tinkerer;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

public final class TinkererSounds {
    private TinkererSounds() {}

    public static final SoundEvent CHARM_EQUIP = register("item.charm.equip");

    private static SoundEvent register(String path) {
        return Registry.register(BuiltInRegistries.SOUND_EVENT, Tinkerer.id(path),
                SoundEvent.createVariableRangeEvent(Tinkerer.id(path)));
    }

    public static void register() {
        Tinkerer.LOGGER.info("Registering tinkerer sounds");
    }
}
