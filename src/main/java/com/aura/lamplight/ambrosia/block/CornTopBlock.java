package com.aura.lamplight.ambrosia.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class CornTopBlock extends TallCropTopBlock {
    public static final MapCodec<CornTopBlock> CODEC = BlockBehaviour.simpleCodec(CornTopBlock::new);

    public CornTopBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends CornTopBlock> codec() {
        return CODEC;
    }

    @Override
    protected Block bottomBlock() {
        return ModBlocks.CORN_CROP;
    }

    @Override
    protected IntegerProperty ageProperty() {
        return BlockStateProperties.AGE_7;
    }
}
