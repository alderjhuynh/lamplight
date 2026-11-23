package com.aura.lamplight.nullbloom;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

public final class NullbloomSounds {
	private NullbloomSounds() {
	}

	public static final SoundEvent NULLBLOOM_LOOP = register("ambient.nullbloom.loop");
	public static final SoundEvent ABYSS_QUAKE = register("ambient.nullbloom.abyss_quake");
	public static final SoundEvent GOD_WHISPER = register("ambient.nullbloom.god_whisper");

	private static SoundEvent register(String path) {
		return Registry.register(BuiltInRegistries.SOUND_EVENT, Nullbloom.id(path),
			SoundEvent.createVariableRangeEvent(Nullbloom.id(path)));
	}

	public static void register() {
		Nullbloom.LOGGER.info("Registering nullbloom sounds");
	}
}
