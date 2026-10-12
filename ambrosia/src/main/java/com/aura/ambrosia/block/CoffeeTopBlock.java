package com.aura.ambrosia.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class CoffeeTopBlock extends TallCropTopBlock {
    public static final MapCodec<CoffeeTopBlock> CODEC = BlockBehaviour.simpleCodec(CoffeeTopBlock::new);

    public CoffeeTopBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends CoffeeTopBlock> codec() {
        return CODEC;
    }

    @Override
    protected Block bottomBlock() {
        return ModBlocks.COFFEE_CROP;
    }

    @Override
    protected IntegerProperty ageProperty() {
        return BlockStateProperties.AGE_7;
    }
}
