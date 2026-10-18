package com.aura.lamplight.ambrosia.block;

import com.aura.lamplight.ambrosia.Ambrosia;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.registry.TillableBlockRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public final class ModBlocks {
    private ModBlocks() {}

    public static final ResourceKey<Block> SOUL_FARMLAND_KEY = blockKey("soul_farmland");
    public static final ResourceKey<Block> SOUL_PEPPER_CROP_KEY = blockKey("soul_pepper_crop");
    public static final ResourceKey<Block> CORN_CROP_KEY = blockKey("corn_crop");
    public static final ResourceKey<Block> RICE_CROP_KEY = blockKey("rice_crop");
    public static final ResourceKey<Block> TOMATO_CROP_KEY = blockKey("tomato_crop");
    public static final ResourceKey<Block> COFFEE_CROP_KEY = blockKey("coffee_crop");
    public static final ResourceKey<Block> LETTUCE_CROP_KEY = blockKey("lettuce_crop");
    public static final ResourceKey<Block> GRAPE_CROP_KEY = blockKey("grape_crop");
    public static final ResourceKey<Block> COOKING_POT_KEY = blockKey("cooking_pot");
    public static final ResourceKey<Block> WINE_RACK_KEY = blockKey("wine_rack");
    public static final ResourceKey<Block> CUTTING_BOARD_KEY = blockKey("cutting_board");
    public static final ResourceKey<Block> MIXING_BOWL_KEY = blockKey("mixing_bowl");
    public static final ResourceKey<Block> PLATE_KEY = blockKey("plate");
    public static final ResourceKey<Block> CORN_TOP_KEY = blockKey("corn_top");
    public static final ResourceKey<Block> RICE_TOP_KEY = blockKey("rice_top");
    public static final ResourceKey<Block> TOMATO_TOP_KEY = blockKey("tomato_top");
    public static final ResourceKey<Block> COFFEE_TOP_KEY = blockKey("coffee_top");

    public static final Block SOUL_FARMLAND = register("soul_farmland", new SoulFarmlandBlock(
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(0.6F)
                    .sound(SoundType.SOUL_SOIL)
                    .randomTicks()
                    .isViewBlocking((state, level, pos) -> true)
                    .isSuffocating((state, level, pos) -> true)
                    .setId(SOUL_FARMLAND_KEY)));

    public static final Block SOUL_PEPPER_CROP = register("soul_pepper_crop", new SoulPepperCropBlock(
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .randomTicks()
                    .instabreak()
                    .sound(SoundType.CROP)
                    .pushReaction(PushReaction.DESTROY)
                    .setId(SOUL_PEPPER_CROP_KEY)));

    public static final Block CORN_CROP = register("corn_crop", new CornCropBlock(cropProperties(CORN_CROP_KEY)));
    public static final Block RICE_CROP = register("rice_crop", new RiceCropBlock(cropProperties(RICE_CROP_KEY)));
    public static final Block TOMATO_CROP = register("tomato_crop", new TomatoCropBlock(cropProperties(TOMATO_CROP_KEY)));
    public static final Block COFFEE_CROP = register("coffee_crop", new CoffeeCropBlock(cropProperties(COFFEE_CROP_KEY)));
    public static final Block LETTUCE_CROP = register("lettuce_crop", new LettuceCropBlock(cropProperties(LETTUCE_CROP_KEY)));
    public static final Block GRAPE_CROP = register("grape_crop", new GrapeCropBlock(cropProperties(GRAPE_CROP_KEY)));

    public static final Block COOKING_POT = register("cooking_pot", new CookingPotBlock(
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.0F, 6.0F)
                    .sound(SoundType.METAL)
                    .setId(COOKING_POT_KEY)));

    public static final Block WINE_RACK = register("wine_rack", new WineRackBlock(
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.WOOD)
                    .setId(WINE_RACK_KEY)));

    public static final Block CUTTING_BOARD = register("cutting_board", new CuttingBoardBlock(
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(1.0F, 2.0F)
                    .sound(SoundType.WOOD)
                    .setId(CUTTING_BOARD_KEY)));

    public static final Block MIXING_BOWL = register("mixing_bowl", new Block(
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(1.0F, 2.0F)
                    .sound(SoundType.WOOD)
                    .setId(MIXING_BOWL_KEY)) {
        @Override
        public MapCodec<? extends Block> codec() {
            return BlockBehaviour.simpleCodec(properties -> ModBlocks.MIXING_BOWL);
        }
    });

    public static final Block PLATE = register("plate", new Block(
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(0.8F, 2.0F)
                    .sound(SoundType.STONE)
                    .setId(PLATE_KEY)) {
        @Override
        public MapCodec<? extends Block> codec() {
            return BlockBehaviour.simpleCodec(properties -> ModBlocks.PLATE);
        }
    });

    public static final Block CORN_TOP = register("corn_top", new CornTopBlock(topCropProperties(CORN_TOP_KEY)));
    public static final Block RICE_TOP = register("rice_top", new RiceTopBlock(topCropProperties(RICE_TOP_KEY)));
    public static final Block TOMATO_TOP = register("tomato_top", new TomatoTopBlock(topCropProperties(TOMATO_TOP_KEY)));
    public static final Block COFFEE_TOP = register("coffee_top", new CoffeeTopBlock(topCropProperties(COFFEE_TOP_KEY)));

    private static BlockBehaviour.Properties topCropProperties(ResourceKey<Block> key) {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.PLANT)
                .noCollision()
                .instabreak()
                .sound(SoundType.CROP)
                .pushReaction(PushReaction.DESTROY)
                .setId(key);
    }

    private static BlockBehaviour.Properties cropProperties(ResourceKey<Block> key) {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.PLANT)
                .noCollision()
                .randomTicks()
                .instabreak()
                .sound(SoundType.CROP)
                .pushReaction(PushReaction.DESTROY)
                .setId(key);
    }

    private static ResourceKey<Block> blockKey(String path) {
        return ResourceKey.create(BuiltInRegistries.BLOCK.key(), Identifier.fromNamespaceAndPath(Ambrosia.MOD_ID, path));
    }

    private static Block register(String path, Block block) {
        return Registry.register(BuiltInRegistries.BLOCK, Identifier.fromNamespaceAndPath(Ambrosia.MOD_ID, path), block);
    }

    public static void register() {
        TillableBlockRegistry.register(Blocks.SOUL_SOIL, HoeItem::onlyIfAirAbove, SOUL_FARMLAND.defaultBlockState());
        Ambrosia.LOGGER.info("Registering {} blocks for {}", 17, Ambrosia.MOD_ID);
    }
}
