package com.aura.lamplight.picturesque.client.ambient;

import com.aura.lamplight.picturesque.PicturesqueSounds;
import com.aura.lamplight.picturesque.client.ambient.AmbientLayer.Gate;
import com.aura.lamplight.picturesque.client.ambient.AmbientLayer.Time;

import java.util.List;

final class AmbientLayers {
	private AmbientLayers() {}

	static List<AmbientLayer> create() {
		return List.of(
				new AmbientLayer("wind_temperate", PicturesqueSounds.WIND_TEMPERATE, true)
						.biome(AmbientTags.WIND_TEMPERATE, 0.81F, Gate.SURFACE).rain(0.3F),
				new AmbientLayer("wind_cold", PicturesqueSounds.WIND_COLD, true)
						.biome(AmbientTags.WIND_COLD, 0.67F, Gate.SURFACE).rain(0.3F),
				new AmbientLayer("wind_arid", PicturesqueSounds.WIND_ARID, true)
						.biome(AmbientTags.WIND_ARID, 0.58F, Gate.SURFACE),
				new AmbientLayer("wind_mountains", PicturesqueSounds.WIND_MOUNTAINS, true)
						.biome(AmbientTags.WIND_MOUNTAIN, 0.27F, Gate.SURFACE).rain(0.3F)
						.extra(c -> 0.27F * AmbientContext.smooth((float) (c.eyeY - 105.0) / 45.0F) * c.surfaceGate()),

				new AmbientLayer("waves", PicturesqueSounds.WATER_WAVES, true)
						.biome(AmbientTags.SHORE, 1.00F, Gate.SURFACE)
						.biome(AmbientTags.OCEAN, 0.65F, Gate.SURFACE).rain(0.15F),
				new AmbientLayer("river", PicturesqueSounds.WATER_RIVER, true)
						.biome(AmbientTags.RIVER, 1.00F, Gate.SURFACE),
				new AmbientLayer("waterfall", PicturesqueSounds.WATER_WATERFALL, false).positional()
						.extra(c -> c.fallCount <= 0 ? 0.0F
								: Math.min(1.0F, 0.4F + c.fallCount / 30.0F) * (c.submerged ? 0.3F : 1.0F)),

				new AmbientLayer("leaves", PicturesqueSounds.FOLIAGE_LEAVES, true)
						.biome(AmbientTags.LEAFY, 0.22F, Gate.SURFACE).rain(0.3F)
						.extra(c -> 0.48F * AmbientContext.smooth(c.leafCount / 45.0F) * c.surfaceGate()),
				new AmbientLayer("cave_leaves", PicturesqueSounds.FOLIAGE_CAVE_LEAVES, true)
						.biome(AmbientTags.CAVE_LEAVES, 0.47F, Gate.NONE)
						.extra(c -> 0.47F * AmbientContext.smooth(c.leafCount / 30.0F) * c.cave),

				new AmbientLayer("birds", PicturesqueSounds.ANIMAL_BIRDS, true)
						.biome(AmbientTags.BIRDS, 0.58F, Gate.SURFACE).time(Time.DAY).rain(-1.0F),
				new AmbientLayer("bees", PicturesqueSounds.ANIMAL_BEES, true)
						.biome(AmbientTags.BEES, 0.39F, Gate.SURFACE).time(Time.DAY).rain(-1.0F)
						.extra(c -> Math.min(1.0F, c.beeCount / 3.0F) * 0.56F),
				new AmbientLayer("cicadas", PicturesqueSounds.ANIMAL_CICADAS, true)
						.biome(AmbientTags.CICADAS, 0.64F, Gate.SURFACE).time(Time.DAY).rain(-1.0F),
				new AmbientLayer("crickets", PicturesqueSounds.ANIMAL_CRICKETS, true)
						.biome(AmbientTags.CRICKETS, 0.68F, Gate.SURFACE).time(Time.NIGHT).rain(-1.0F),
				new AmbientLayer("frogs", PicturesqueSounds.ANIMAL_FROGS_AND_CRICKETS, false)
						.biome(AmbientTags.FROGS, 0.78F, Gate.SURFACE).time(Time.NIGHT_OR_RAIN),
				new AmbientLayer("mangrove_birds", PicturesqueSounds.ANIMAL_MANGROVE_BIRDS, true)
						.biome(AmbientTags.MANGROVE_BIRDS, 0.79F, Gate.SURFACE).time(Time.DAY).rain(-0.8F),
				new AmbientLayer("jungle_day", PicturesqueSounds.ANIMAL_JUNGLE_DAY, true)
						.biome(AmbientTags.JUNGLE, 0.62F, Gate.SURFACE).time(Time.DAY).rain(-0.4F),
				new AmbientLayer("jungle_night", PicturesqueSounds.ANIMAL_JUNGLE_NIGHT, true)
						.biome(AmbientTags.JUNGLE, 0.78F, Gate.SURFACE).time(Time.NIGHT).rain(-0.3F),
				new AmbientLayer("owls", PicturesqueSounds.ANIMAL_OWLS, true)
						.biome(AmbientTags.OWLS, 0.41F, Gate.SURFACE).time(Time.NIGHT).rain(-1.0F),

				new AmbientLayer("wind_cave", PicturesqueSounds.WIND_CAVE, true)
						.biome(AmbientTags.CAVE, 0.38F, Gate.NONE)
						.extra(c -> 0.32F * c.cave
								* (1.0F - c.maxShare(AmbientTags.DRIPSTONE, AmbientTags.LUSH_CAVES, AmbientTags.DEEP_DARK))),
				new AmbientLayer("dripstone_caves", PicturesqueSounds.WATER_DRIPSTONE_CAVES, true)
						.biome(AmbientTags.DRIPSTONE, 0.81F, Gate.NONE),
				new AmbientLayer("lush_caves", PicturesqueSounds.WATER_LUSH_CAVES, true)
						.biome(AmbientTags.LUSH_CAVES, 0.79F, Gate.NONE),

				new AmbientLayer("deep_dark", PicturesqueSounds.WIND_DEEP_DARK, false)
						.biome(AmbientTags.DEEP_DARK, 0.35F, Gate.NONE)
						.biome(AmbientTags.EERIE_SURFACE, 0.14F, Gate.SURFACE),
				new AmbientLayer("end", PicturesqueSounds.WIND_END, false)
						.biome(AmbientTags.END, 0.44F, Gate.NONE)
		);
	}
}
