package com.aura.lamplight.ambrosia.sound;

import com.aura.lamplight.ambrosia.Ambrosia;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public final class ModSounds {
    private ModSounds() {}

    public static final SoundEvent POT_BOIL_SOUP = register("block.cooking_pot.boil_soup");
    public static final SoundEvent POT_BOIL_WATER = register("block.cooking_pot.boil_water");

    private static SoundEvent register(String path) {
        Identifier id = Identifier.fromNamespaceAndPath(Ambrosia.MOD_ID, path);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void register() {
        Ambrosia.LOGGER.info("Registering sounds for {}", Ambrosia.MOD_ID);
    }
}
