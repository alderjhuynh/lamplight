package com.aura.lamplight.menagerie.registry;

import com.aura.lamplight.menagerie.Menagerie;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public final class MenagerieSounds {
    public static final SoundEvent CROW_IDLE = create("crow_idle");
    public static final SoundEvent CROW_HURT = create("crow_hurt");
    public static final SoundEvent RACCOON_IDLE = create("raccoon_idle");
    public static final SoundEvent RACCOON_HURT = create("raccoon_hurt");
    public static final SoundEvent WISP_IDLE = create("wisp_idle");
    public static final SoundEvent WISP_HURT = create("wisp_hurt");
    public static final SoundEvent WISP_PULSE = create("wisp_pulse");
    public static final SoundEvent WISP_TAME = create("wisp_tame");

    private MenagerieSounds() {
    }

    private static SoundEvent create(String name) {
        Identifier id = Menagerie.id(name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void init() {
    }
}
