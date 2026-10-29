package com.aura.lamplight.hearthwright.block;

import com.aura.lamplight.hearthwright.Hearthwright;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;

public final class StoneBlocks {
    private static final List<Item> ITEMS = new ArrayList<>();

    private StoneBlocks() {
    }

    public static void register() {
        add("stone", Blocks.STONE, false, false, true);
        add("purpur", Blocks.PURPUR_BLOCK, false, false, true);
        add("quartz", Blocks.QUARTZ_BLOCK, false, false, true);
        add("prismarine_brick", Blocks.PRISMARINE_BRICKS, false, false, true);
        add("dark_prismarine", Blocks.DARK_PRISMARINE, false, false, true);

        add("deepslate", Blocks.DEEPSLATE, true, true, true);
        add("packed_mud", Blocks.PACKED_MUD, true, true, true);
        add("netherrack", Blocks.NETHERRACK, true, true, true);
        add("basalt", Blocks.BASALT, true, true, true);
        add("quartz_brick", Blocks.QUARTZ_BRICKS, true, true, true);

        add("polished_andesite", Blocks.POLISHED_ANDESITE, false, false, true);
        add("polished_diorite", Blocks.POLISHED_DIORITE, false, false, true);
        add("polished_granite", Blocks.POLISHED_GRANITE, false, false, true);
        add("polished_basalt", Blocks.POLISHED_BASALT, true, true, true);

        add("cut_sandstone", Blocks.CUT_SANDSTONE, false, true, true);
        add("cut_red_sandstone", Blocks.CUT_RED_SANDSTONE, false, true, true);
        add("smooth_stone", Blocks.SMOOTH_STONE, false, true, true);
        add("smooth_basalt", Blocks.SMOOTH_BASALT, true, true, true);
        add("smooth_red_sandstone", Blocks.SMOOTH_RED_SANDSTONE, false, false, true);
        add("smooth_sandstone", Blocks.SMOOTH_SANDSTONE, false, false, true);
        add("end_stone", Blocks.END_STONE, true, true, true);

        Hearthwright.LOGGER.info("Registered {} stone derivative blocks", ITEMS.size());
    }

    public static void addCreativeTabEntries() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.BUILDING_BLOCKS)
                .register(output -> ITEMS.forEach(output::accept));
    }

    private static void add(String name, Block base, boolean slab, boolean stairs, boolean wall) {
        add(ITEMS, name, base, slab, stairs, wall);
    }

    static void add(
            List<Item> items, String name, Block base, boolean slab, boolean stairs, boolean wall) {
        if (slab) {
            ResourceKey<Block> key = HearthwrightBlocks.blockKey(name + "_slab");
            Block block = Registry.register(
                    BuiltInRegistries.BLOCK,
                    key,
                    new SlabBlock(HearthwrightBlocks.derivativeProperties(base).setId(key)));
            items.add(HearthwrightBlocks.registerBlockItem(name + "_slab", block));
        }

        if (stairs) {
            ResourceKey<Block> key = HearthwrightBlocks.blockKey(name + "_stairs");
            Block block = Registry.register(
                    BuiltInRegistries.BLOCK,
                    key,
                    new StairBlock(base.defaultBlockState(), HearthwrightBlocks.derivativeProperties(base).setId(key)));
            items.add(HearthwrightBlocks.registerBlockItem(name + "_stairs", block));
        }

        if (wall) {
            ResourceKey<Block> key = HearthwrightBlocks.blockKey(name + "_wall");
            Block block = Registry.register(
                    BuiltInRegistries.BLOCK,
                    key,
                    new WallBlock(HearthwrightBlocks.derivativeProperties(base).forceSolidOn().setId(key)));
            items.add(HearthwrightBlocks.registerBlockItem(name + "_wall", block));
        }
    }
}
