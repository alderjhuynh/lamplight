package com.aura.ambrosia.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class RiceTopBlock extends TallCropTopBlock {
    public static final MapCodec<RiceTopBlock> CODEC = BlockBehaviour.simpleCodec(RiceTopBlock::new);

    public RiceTopBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends RiceTopBlock> codec() {
        return CODEC;
    }

    @Override
    protected Block bottomBlock() {
        return ModBlocks.RICE_CROP;
    }

    @Override
    protected IntegerProperty ageProperty() {
        return RiceCropBlock.AGE_0_5;
    }
}
