package com.aura.lamplight.wayfarer;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

public final class WayfarerSounds {
	private WayfarerSounds() {}

	public static final SoundEvent BACKPACK_OPEN = register("item.leather_backpack.open");

	public static final SoundEvent BACKPACK_CLOSE = register("item.leather_backpack.close");

	private static SoundEvent register(String path) {
		return Registry.register(BuiltInRegistries.SOUND_EVENT, Wayfarer.id(path),
				SoundEvent.createVariableRangeEvent(Wayfarer.id(path)));
	}

	public static void register() {
		Wayfarer.LOGGER.info("Registered wayfarer sounds");
	}
}
