package com.aura.lamplight.hearthwright.block;

import com.aura.lamplight.hearthwright.Hearthwright;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;

public final class ConcreteBlocks {
    private static final List<Variant> VARIANTS = new ArrayList<>();

    private ConcreteBlocks() {
    }

    public record Variant(String baseName, Block slab, Block stairs, Item slabItem, Item stairsItem) {
    }

    public static void register() {
        for (DyeColor color : DyeColor.VALUES) {
            addVariant(color.getName() + "_concrete", Blocks.CONCRETE.pick(color));
        }

        Hearthwright.LOGGER.info("Registered {} concrete slabs and stairs", VARIANTS.size() * 2);
    }

    public static void addCreativeTabEntries() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.BUILDING_BLOCKS).register(output -> {
            for (Variant variant : VARIANTS) {
                output.accept(variant.slabItem());
                output.accept(variant.stairsItem());
            }
        });

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COLORED_BLOCKS).register(output -> {
            for (Variant variant : VARIANTS) {
                output.accept(variant.slabItem());
                output.accept(variant.stairsItem());
            }
        });
    }

    private static void addVariant(String baseName, Block baseBlock) {
        ResourceKey<Block> slabKey = HearthwrightBlocks.blockKey(baseName + "_slab");
        ResourceKey<Block> stairsKey = HearthwrightBlocks.blockKey(baseName + "_stairs");

        Block slab = Registry.register(
                BuiltInRegistries.BLOCK,
                slabKey,
                new SlabBlock(HearthwrightBlocks.derivativeProperties(baseBlock).setId(slabKey)));

        Block stairs = Registry.register(
                BuiltInRegistries.BLOCK,
                stairsKey,
                new StairBlock(baseBlock.defaultBlockState(), HearthwrightBlocks.derivativeProperties(baseBlock).setId(stairsKey)));

        VARIANTS.add(new Variant(
                baseName,
                slab,
                stairs,
                HearthwrightBlocks.registerBlockItem(baseName + "_slab", slab),
                HearthwrightBlocks.registerBlockItem(baseName + "_stairs", stairs)));
    }
}
