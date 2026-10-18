package com.aura.lamplight.ambrosia.block;

import com.aura.lamplight.ambrosia.item.ModItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class TomatoCropBlock extends AmbrosiaTallCropBlock {
    public static final MapCodec<TomatoCropBlock> CODEC = BlockBehaviour.simpleCodec(TomatoCropBlock::new);
    public static final IntegerProperty AGE_0_6 = IntegerProperty.create("age", 0, 6);

    public TomatoCropBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends TomatoCropBlock> codec() {
        return CODEC;
    }

    @Override
    protected IntegerProperty getAgeProperty() {
        return AGE_0_6;
    }

    @Override
    public int getMaxAge() {
        return 6;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModItems.TOMATO_SEEDS;
    }

    @Override
    protected Block topBlock() {
        return ModBlocks.TOMATO_TOP;
    }

    @Override
    protected IntegerProperty topAge() {
        return AGE_0_6;
    }
}
