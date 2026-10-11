package com.aura.menagerie.registry;

import com.aura.menagerie.Menagerie;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public final class MenagerieSounds {
    public static final SoundEvent CROW_IDLE = create("crow_idle");
    public static final SoundEvent CROW_HURT = create("crow_hurt");

    private MenagerieSounds() {
    }

    private static SoundEvent create(String name) {
        Identifier id = Menagerie.id(name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void init() {
    }
}
