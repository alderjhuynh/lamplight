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
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class SandstoneBlocks {
    private static final List<Item> ITEMS = new ArrayList<>();

    private SandstoneBlocks() {
    }

    public static void register() {
        Block cobbledSandstone = registerBase(Blocks.STONE, "cobbled_sandstone");
        registerBase(Blocks.STONE, "mossy_cobbled_sandstone");
        Block sandstoneBricks = registerBase(Blocks.SANDSTONE, "sandstone_bricks");
        registerBase(Blocks.SANDSTONE, "cracked_sandstone_bricks");
        registerBase(Blocks.SANDSTONE, "mossy_sandstone_bricks");

        StoneBlocks.add(ITEMS, "cobbled_sandstone", cobbledSandstone, true, true, true);
        StoneBlocks.add(ITEMS, "mossy_cobbled_sandstone", cobbledSandstone, true, true, true);
        StoneBlocks.add(ITEMS, "sandstone_brick", sandstoneBricks, true, true, true);
        StoneBlocks.add(ITEMS, "mossy_sandstone_brick", sandstoneBricks, true, true, true);

        Hearthwright.LOGGER.info("Registered {} sandstone blocks", ITEMS.size());
    }

    public static void addCreativeTabEntries() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.BUILDING_BLOCKS)
                .register(output -> ITEMS.forEach(output::accept));
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
