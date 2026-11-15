package com.aura.lamplight.picturesque;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

public final class PicturesqueSounds {
	private PicturesqueSounds() {}

	public static final SoundEvent ANIMAL_BEES = register("ambient.animal.bees");
	public static final SoundEvent ANIMAL_BIRDS = register("ambient.animal.birds");
	public static final SoundEvent ANIMAL_CICADAS = register("ambient.animal.cicadas");
	public static final SoundEvent ANIMAL_CRICKETS = register("ambient.animal.crickets");
	public static final SoundEvent ANIMAL_FROGS_AND_CRICKETS = register("ambient.animal.frogs_and_crickets");
	public static final SoundEvent ANIMAL_JUNGLE_DAY = register("ambient.animal.jungle_day");
	public static final SoundEvent ANIMAL_JUNGLE_NIGHT = register("ambient.animal.jungle_night");
	public static final SoundEvent ANIMAL_MANGROVE_BIRDS = register("ambient.animal.mangrove_birds");
	public static final SoundEvent ANIMAL_OWLS = register("ambient.animal.owls");

	public static final SoundEvent FOLIAGE_CAVE_LEAVES = register("ambient.foliage.cave_leaves");
	public static final SoundEvent FOLIAGE_LEAVES = register("ambient.foliage.leaves");

	public static final SoundEvent WATER_DRIPSTONE_CAVES = register("ambient.water.dripstone_caves");
	public static final SoundEvent WATER_LUSH_CAVES = register("ambient.water.lush_caves");
	public static final SoundEvent WATER_RIVER = register("ambient.water.river");
	public static final SoundEvent WATER_WAVES = register("ambient.water.waves");
	public static final SoundEvent WATER_WATERFALL = register("ambient.water.waterfall");

	public static final SoundEvent WIND_ARID = register("ambient.wind.arid");
	public static final SoundEvent WIND_CAVE = register("ambient.wind.cave");
	public static final SoundEvent WIND_COLD = register("ambient.wind.cold");
	public static final SoundEvent WIND_DEEP_DARK = register("ambient.wind.deep_dark");
	public static final SoundEvent WIND_END = register("ambient.wind.end");
	public static final SoundEvent WIND_MOUNTAINS = register("ambient.wind.mountains");
	public static final SoundEvent WIND_TEMPERATE = register("ambient.wind.temperate");

	private static SoundEvent register(String path) {
		return Registry.register(BuiltInRegistries.SOUND_EVENT, Picturesque.id(path),
				SoundEvent.createVariableRangeEvent(Picturesque.id(path)));
	}

	public static void init() {
		Picturesque.LOGGER.info("Registered picturesque ambient sounds");
	}
}
