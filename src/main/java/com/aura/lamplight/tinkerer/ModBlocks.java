package com.aura.lamplight.tinkerer;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class ModBlocks {
    private ModBlocks() {
    }

    public static final ResourceKey<Block> JEWELING_TABLE_KEY =
        ResourceKey.create(Registries.BLOCK, Tinkerer.id("jeweling_table"));

    public static final Block JEWELING_TABLE = Registry.register(
        BuiltInRegistries.BLOCK,
        JEWELING_TABLE_KEY,
        new JewelingTableBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.WOOD)
            .strength(2.5F)
            .sound(SoundType.WOOD)
            .setId(JEWELING_TABLE_KEY)));

    public static void register() {
        Tinkerer.LOGGER.info("Registered tinkerer blocks");
    }
}
