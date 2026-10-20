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
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

/**
 * Slabs and stairs for every terracotta block in the game.
 *
 * <p>The models for these blocks point at vanilla's own terracotta textures, so the
 * whole family is generated without adding a single new texture.</p>
 */
public final class TerracottaBlocks {
    private static final List<Variant> VARIANTS = new ArrayList<>();

    private TerracottaBlocks() {
    }

    public record Variant(String baseName, Block slab, Block stairs, Item slabItem, Item stairsItem) {
    }

    public static void register() {
        addVariant("terracotta", Blocks.TERRACOTTA, MapColor.COLOR_ORANGE);
        for (DyeColor color : DyeColor.VALUES) {
            addVariant(color.getName() + "_terracotta", Blocks.DYED_TERRACOTTA.pick(color), color.getTerracottaColor());
        }

        Hearthwright.LOGGER.info("Registered {} terracotta slabs and stairs", VARIANTS.size() * 2);
    }

    public static void addCreativeTabEntries() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.BUILDING_BLOCKS).register(output -> {
            for (Variant variant : VARIANTS) {
                output.accept(variant.slabItem());
                output.accept(variant.stairsItem());
            }
        });

        // The coloured terracotta family also belongs alongside vanilla's coloured blocks.
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COLORED_BLOCKS).register(output -> {
            for (Variant variant : VARIANTS) {
                if (!variant.baseName().equals("terracotta")) {
                    output.accept(variant.slabItem());
                    output.accept(variant.stairsItem());
                }
            }
        });
    }

    private static void addVariant(String baseName, Block baseBlock, MapColor mapColor) {
        ResourceKey<Block> slabKey = HearthwrightBlocks.blockKey(baseName + "_slab");
        ResourceKey<Block> stairsKey = HearthwrightBlocks.blockKey(baseName + "_stairs");

        Block slab = Registry.register(
                BuiltInRegistries.BLOCK,
                slabKey,
                new SlabBlock(terracottaProperties(mapColor).setId(slabKey)));

        Block stairs = Registry.register(
                BuiltInRegistries.BLOCK,
                stairsKey,
                new StairBlock(baseBlock.defaultBlockState(), terracottaProperties(mapColor).setId(stairsKey)));

        Item slabItem = HearthwrightBlocks.registerBlockItem(baseName + "_slab", slab);
        Item stairsItem = HearthwrightBlocks.registerBlockItem(baseName + "_stairs", stairs);

        VARIANTS.add(new Variant(baseName, slab, stairs, slabItem, stairsItem));
    }

    private static BlockBehaviour.Properties terracottaProperties(MapColor mapColor) {
        return BlockBehaviour.Properties.of()
                .mapColor(mapColor)
                .instrument(NoteBlockInstrument.BASEDRUM)
                .requiresCorrectToolForDrops()
                .strength(1.25F, 4.2F);
    }
}
