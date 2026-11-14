package com.aura.lamplight.ambrosia;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

public final class AmbrosiaSounds {
    private AmbrosiaSounds() {}

    public static final SoundEvent COOKING_POT_INSERT = register("block.cooking_pot.insert");
    public static final SoundEvent COOKING_POT_COLLECT = register("block.cooking_pot.collect");
    public static final SoundEvent COOKING_POT_AMBIENT = register("block.cooking_pot.ambient");

    private static SoundEvent register(String path) {
        return Registry.register(BuiltInRegistries.SOUND_EVENT, Ambrosia.id(path),
                SoundEvent.createVariableRangeEvent(Ambrosia.id(path)));
    }

    public static void register() {
        Ambrosia.LOGGER.info("Registering ambrosia sounds");
    }
}
