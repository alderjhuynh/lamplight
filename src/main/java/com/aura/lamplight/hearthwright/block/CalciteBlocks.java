package com.aura.lamplight.hearthwright.block;

import com.aura.lamplight.hearthwright.Hearthwright;
import com.mojang.serialization.MapCodec;
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
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class CalciteBlocks {
    private static final List<Item> ITEMS = new ArrayList<>();

    private CalciteBlocks() {
    }

    public static void register() {
        Block polishedCalcite = registerBase(Blocks.CALCITE, "polished_calcite");
        Block polishedCalciteBricks = registerBase(Blocks.CALCITE, "polished_calcite_bricks");
        registerPillar(Blocks.CALCITE, "calcite_pillar");

        StoneBlocks.add(ITEMS, "polished_calcite", polishedCalcite, true, true, true);
        StoneBlocks.add(ITEMS, "polished_calcite_brick", polishedCalciteBricks, true, true, true);

        Hearthwright.LOGGER.info("Registered {} calcite blocks", ITEMS.size());
    }

    public static void addCreativeTabEntries() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.BUILDING_BLOCKS)
                .register(output -> ITEMS.forEach(output::accept));
    }

    private static Block registerPillar(Block base, String name) {
        ResourceKey<Block> key = HearthwrightBlocks.blockKey(name);
        Block block = Registry.register(
                BuiltInRegistries.BLOCK,
                key,
                new RotatedPillarBlock(HearthwrightBlocks.derivativeProperties(base).setId(key)));
        ITEMS.add(HearthwrightBlocks.registerBlockItem(name, block));
        return block;
    }

    private static Block registerBase(Block base, String name) {
        ResourceKey<Block> key = HearthwrightBlocks.blockKey(name);
        Block block = Registry.register(
                BuiltInRegistries.BLOCK,
                key,
                new Block(HearthwrightBlocks.derivativeProperties(base).setId(key)) {
                    @Override
                    public MapCodec<? extends Block> codec() {
                        return BlockBehaviour.simpleCodec(properties -> this);
                    }
                });
        ITEMS.add(HearthwrightBlocks.registerBlockItem(name, block));
        return block;
    }
}
