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

public final class AmethystBlocks {
    private static final List<Item> ITEMS = new ArrayList<>();

    private AmethystBlocks() {
    }

    public static void register() {
        Block amethystBricks = registerBase("amethyst_bricks");
        registerPillar("amethyst_pillar");
        registerBase("chiseled_amethyst");

        StoneBlocks.add(ITEMS, "amethyst_brick", amethystBricks, true, true, true);

        Hearthwright.LOGGER.info("Registered {} amethyst blocks", ITEMS.size());
    }

    public static void addCreativeTabEntries() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.BUILDING_BLOCKS)
                .register(output -> ITEMS.forEach(output::accept));
    }

    private static Block registerPillar(String name) {
        ResourceKey<Block> key = HearthwrightBlocks.blockKey(name);
        Block block = Registry.register(
                BuiltInRegistries.BLOCK,
                key,
                new RotatedPillarBlock(
                        HearthwrightBlocks.derivativeProperties(Blocks.AMETHYST_BLOCK).setId(key)));
        ITEMS.add(HearthwrightBlocks.registerBlockItem(name, block));
        return block;
    }

    private static Block registerBase(String name) {
        ResourceKey<Block> key = HearthwrightBlocks.blockKey(name);
        Block block = Registry.register(
                BuiltInRegistries.BLOCK,
                key,
                new Block(HearthwrightBlocks.derivativeProperties(Blocks.AMETHYST_BLOCK).setId(key)) {
                    @Override
                    public MapCodec<? extends Block> codec() {
                        return BlockBehaviour.simpleCodec(properties -> this);
                    }
                });
        ITEMS.add(HearthwrightBlocks.registerBlockItem(name, block));
        return block;
    }
}
