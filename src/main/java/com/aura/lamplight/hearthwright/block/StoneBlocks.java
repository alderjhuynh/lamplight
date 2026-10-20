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
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Missing slab, stair, and wall variants of vanilla's stone-like blocks.
 *
 * <p>Properties are copied from each base block so hardness, explosion resistance,
 * sounds, map colours, and tool requirements all match. Models reuse the base
 * block's textures, including the separate top/side textures of column blocks.</p>
 */
public final class StoneBlocks {
    private static final List<Item> ITEMS = new ArrayList<>();

    private StoneBlocks() {
    }

    public static void register() {
        add("stone", Blocks.STONE, false, false, true);

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

        Hearthwright.LOGGER.info("Registered {} stone derivative blocks", ITEMS.size());
    }

    public static void addCreativeTabEntries() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.BUILDING_BLOCKS)
                .register(output -> ITEMS.forEach(output::accept));
    }

    private static void add(String name, Block base, boolean slab, boolean stairs, boolean wall) {
        if (slab) {
            ResourceKey<Block> key = HearthwrightBlocks.blockKey(name + "_slab");
            Block block = Registry.register(
                    BuiltInRegistries.BLOCK,
                    key,
                    new SlabBlock(derivativeProperties(base).setId(key)));
            ITEMS.add(HearthwrightBlocks.registerBlockItem(name + "_slab", block));
        }

        if (stairs) {
            ResourceKey<Block> key = HearthwrightBlocks.blockKey(name + "_stairs");
            Block block = Registry.register(
                    BuiltInRegistries.BLOCK,
                    key,
                    new StairBlock(base.defaultBlockState(), derivativeProperties(base).setId(key)));
            ITEMS.add(HearthwrightBlocks.registerBlockItem(name + "_stairs", block));
        }

        if (wall) {
            ResourceKey<Block> key = HearthwrightBlocks.blockKey(name + "_wall");
            Block block = Registry.register(
                    BuiltInRegistries.BLOCK,
                    key,
                    new WallBlock(derivativeProperties(base).forceSolidOn().setId(key)));
            ITEMS.add(HearthwrightBlocks.registerBlockItem(name + "_wall", block));
        }
    }

    @SuppressWarnings("deprecation")
    private static BlockBehaviour.Properties derivativeProperties(Block base) {
        return BlockBehaviour.Properties.ofLegacyCopy(base);
    }
}
