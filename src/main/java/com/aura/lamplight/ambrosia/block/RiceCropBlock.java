package com.aura.lamplight.ambrosia.block;

import com.aura.lamplight.ambrosia.item.ModItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class RiceCropBlock extends AmbrosiaTallCropBlock {
    public static final MapCodec<RiceCropBlock> CODEC = BlockBehaviour.simpleCodec(RiceCropBlock::new);
    public static final IntegerProperty AGE_0_5 = IntegerProperty.create("age", 0, 5);

    public RiceCropBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends RiceCropBlock> codec() {
        return CODEC;
    }

    @Override
    protected IntegerProperty getAgeProperty() {
        return AGE_0_5;
    }

    @Override
    public int getMaxAge() {
        return 5;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModItems.RICE_SEEDS;
    }

    @Override
    protected Block topBlock() {
        return ModBlocks.RICE_TOP;
    }

    @Override
    protected IntegerProperty topAge() {
        return AGE_0_5;
    }
}
