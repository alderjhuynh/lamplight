package com.aura.ambrosia.block;

import com.aura.ambrosia.item.ModItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class GrapeCropBlock extends AmbrosiaCropBlock {
    public static final MapCodec<GrapeCropBlock> CODEC = BlockBehaviour.simpleCodec(GrapeCropBlock::new);

    public GrapeCropBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends GrapeCropBlock> codec() {
        return CODEC;
    }

    @Override
    protected IntegerProperty getAgeProperty() {
        return BlockStateProperties.AGE_3;
    }

    @Override
    public int getMaxAge() {
        return 3;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModItems.GRAPE_SEEDS;
    }
}
