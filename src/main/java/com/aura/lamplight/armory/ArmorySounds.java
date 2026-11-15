package com.aura.lamplight.armory;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public final class ArmorySounds {
	private ArmorySounds() {
	}

	public static final SoundEvent CHAKRAM_THROW = create("item.chakram.throw");
	public static final SoundEvent CHAKRAM_WHIRR = create("item.chakram.whirr");
	public static final SoundEvent CHAKRAM_HIT = create("item.chakram.hit");
	public static final SoundEvent CHAKRAM_BOUNCE = create("item.chakram.bounce");
	public static final SoundEvent CHAKRAM_RETURN = create("item.chakram.return");
	public static final SoundEvent CHAKRAM_CATCH = create("item.chakram.catch");

	private static SoundEvent create(final String name) {
		Identifier id = Armory.id(name);
		return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
	}

	public static void initialize() {
		Armory.LOGGER.info("Registered armory sounds");
	}
}