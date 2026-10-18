package com.aura.lamplight.ambrosia.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class TomatoTopBlock extends TallCropTopBlock {
    public static final MapCodec<TomatoTopBlock> CODEC = BlockBehaviour.simpleCodec(TomatoTopBlock::new);

    public TomatoTopBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends TomatoTopBlock> codec() {
        return CODEC;
    }

    @Override
    protected Block bottomBlock() {
        return ModBlocks.TOMATO_CROP;
    }

    @Override
    protected IntegerProperty ageProperty() {
        return TomatoCropBlock.AGE_0_6;
    }
}
