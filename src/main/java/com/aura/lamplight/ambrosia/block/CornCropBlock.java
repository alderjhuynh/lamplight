package com.aura.lamplight.ambrosia.block;

import com.aura.lamplight.ambrosia.item.ModItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class CornCropBlock extends AmbrosiaTallCropBlock {
    public static final MapCodec<CornCropBlock> CODEC = BlockBehaviour.simpleCodec(CornCropBlock::new);

    public CornCropBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends CornCropBlock> codec() {
        return CODEC;
    }

    @Override
    protected IntegerProperty getAgeProperty() {
        return BlockStateProperties.AGE_7;
    }

    @Override
    public int getMaxAge() {
        return 7;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModItems.CORN_SEEDS;
    }

    @Override
    protected Block topBlock() {
        return ModBlocks.CORN_TOP;
    }

    @Override
    protected IntegerProperty topAge() {
        return BlockStateProperties.AGE_7;
    }
}
