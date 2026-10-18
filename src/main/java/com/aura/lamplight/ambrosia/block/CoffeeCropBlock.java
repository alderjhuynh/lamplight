package com.aura.lamplight.ambrosia.block;

import com.aura.lamplight.ambrosia.item.ModItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class CoffeeCropBlock extends AmbrosiaTallCropBlock {
    public static final MapCodec<CoffeeCropBlock> CODEC = BlockBehaviour.simpleCodec(CoffeeCropBlock::new);

    public CoffeeCropBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends CoffeeCropBlock> codec() {
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
        return ModItems.COFFEE_CHERRIES;
    }

    @Override
    protected Block topBlock() {
        return ModBlocks.COFFEE_TOP;
    }

    @Override
    protected IntegerProperty topAge() {
        return BlockStateProperties.AGE_7;
    }
}
