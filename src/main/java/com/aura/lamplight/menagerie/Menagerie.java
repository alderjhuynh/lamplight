package com.aura.lamplight.menagerie;

import com.aura.lamplight.menagerie.registry.MenagerieEntities;
import com.aura.lamplight.menagerie.registry.MenagerieSounds;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTabs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Menagerie implements ModInitializer {
	public static final String MOD_ID = "lamplight";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		MenagerieSounds.init();
		MenagerieEntities.init();
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.SPAWN_EGGS).register(output -> output.accept(MenagerieEntities.CROW_SPAWN_EGG));

		BiomeModifications.addSpawn(
				BiomeSelectors.tag(net.minecraft.tags.BiomeTags.IS_FOREST),
				MobCategory.CREATURE, MenagerieEntities.CROW, 10, 1, 2);
		BiomeModifications.addSpawn(
				BiomeSelectors.tag(net.minecraft.tags.BiomeTags.IS_TAIGA),
				MobCategory.CREATURE, MenagerieEntities.CROW, 8, 1, 2);
		BiomeModifications.addSpawn(
				BiomeSelectors.includeByKey(
						biomeKey("minecraft", "plains"),
						biomeKey("minecraft", "sunflower_plains"),
						biomeKey("minecraft", "meadow"),
						biomeKey("minecraft", "cherry_grove")),
				MobCategory.CREATURE, MenagerieEntities.CROW, 8, 1, 2);
		BiomeModifications.addSpawn(
				BiomeSelectors.includeByKey(
						vantageBiome("steppe"),
						vantageBiome("shrubland"),
						vantageBiome("scrublands"),
						vantageBiome("cold_shrubland"),
						vantageBiome("hanging_meadow"),
						vantageBiome("lush_valley"),
						vantageBiome("basin"),
						vantageBiome("highlands"),
						vantageBiome("mountainous_highlands"),
						vantageBiome("thornscrub"),
						vantageBiome("misty_isles"),
						vantageBiome("burnt_savanna"),
						vantageBiome("shattered_savanna"),
						vantageBiome("savanna_badlands"),
						vantageBiome("savanna_slopes"),
						vantageBiome("yellowstone")),
				MobCategory.CREATURE, MenagerieEntities.CROW, 8, 1, 2);

		LOGGER.info("Menagerie init");
	}

	private static net.minecraft.resources.ResourceKey<net.minecraft.world.level.biome.Biome> vantageBiome(String path) {
		return biomeKey("lamplight", path);
	}

	private static net.minecraft.resources.ResourceKey<net.minecraft.world.level.biome.Biome> biomeKey(String namespace, String path) {
		return net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BIOME,
				Identifier.fromNamespaceAndPath(namespace, path));
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
