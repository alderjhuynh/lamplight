package com.aura.lamplight.picturesque.client.ambient;

import com.aura.lamplight.picturesque.Picturesque;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public final class AmbientTags {
	private AmbientTags() {}

	public static final TagKey<Biome> WIND_TEMPERATE = tag("wind_temperate");
	public static final TagKey<Biome> WIND_COLD = tag("wind_cold");
	public static final TagKey<Biome> WIND_ARID = tag("wind_arid");
	public static final TagKey<Biome> WIND_MOUNTAIN = tag("wind_mountain");

	public static final TagKey<Biome> SHORE = tag("shore");
	public static final TagKey<Biome> OCEAN = tag("ocean");
	public static final TagKey<Biome> RIVER = tag("river");

	public static final TagKey<Biome> LEAFY = tag("leafy");
	public static final TagKey<Biome> BIRDS = tag("birds");
	public static final TagKey<Biome> BEES = tag("bees");
	public static final TagKey<Biome> CICADAS = tag("cicadas");
	public static final TagKey<Biome> CRICKETS = tag("crickets");
	public static final TagKey<Biome> FROGS = tag("frogs");
	public static final TagKey<Biome> MANGROVE_BIRDS = tag("mangrove_birds");
	public static final TagKey<Biome> JUNGLE = tag("jungle");
	public static final TagKey<Biome> OWLS = tag("owls");

	public static final TagKey<Biome> CAVE = tag("cave");
	public static final TagKey<Biome> DRIPSTONE = tag("dripstone");
	public static final TagKey<Biome> LUSH_CAVES = tag("lush_caves");
	public static final TagKey<Biome> CAVE_LEAVES = tag("cave_leaves");
	public static final TagKey<Biome> DEEP_DARK = tag("deep_dark");
	public static final TagKey<Biome> EERIE_SURFACE = tag("eerie_surface");
	public static final TagKey<Biome> END = tag("end");

	private static TagKey<Biome> tag(String path) {
		return TagKey.create(Registries.BIOME, Picturesque.id("ambient/" + path));
	}
}
