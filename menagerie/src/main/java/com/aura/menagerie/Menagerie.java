package com.aura.menagerie;

import com.aura.menagerie.registry.MenagerieEntities;
import com.aura.menagerie.registry.MenagerieSounds;
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
	public static final String MOD_ID = "menagerie";

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
				BiomeSelectors.includeByKey(
						net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BIOME,
								Identifier.fromNamespaceAndPath("minecraft", "plains")),
						net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BIOME,
								Identifier.fromNamespaceAndPath("minecraft", "sunflower_plains"))),
				MobCategory.CREATURE, MenagerieEntities.CROW, 8, 1, 2);

		LOGGER.info("Menagerie init");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
