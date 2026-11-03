package com.aura.lamplight.wayfarer.waystone;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class WaystoneBlockEntity extends BlockEntity {
    public WaystoneBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WAYSTONE, pos, state);
    }
}
