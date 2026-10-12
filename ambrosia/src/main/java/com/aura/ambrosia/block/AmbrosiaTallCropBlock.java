package com.aura.ambrosia.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class AmbrosiaTallCropBlock extends CropBlock {
    private VoxelShape[] shapes;

    protected AmbrosiaTallCropBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    protected abstract Block topBlock();

    protected abstract IntegerProperty topAge();

    @Override
    public BlockState getStateForAge(int age) {
        return this.defaultBlockState().setValue(this.getAgeProperty(), age);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        this.syncTop(level, pos);
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        super.performBonemeal(level, random, pos, state);
        this.syncTop(level, pos);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        this.syncTop(level, pos);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (!super.canSurvive(state, level, pos)) {
            return false;
        }
        BlockState above = level.getBlockState(pos.above());
        return above.isAir() || above.is(this.topBlock());
    }

    protected void syncTop(Level level, BlockPos pos) {
        if (level.isClientSide()) {
            return;
        }
        BlockState self = level.getBlockState(pos);
        if (!self.is(this)) {
            return;
        }
        BlockState want = this.topBlock().defaultBlockState().setValue(this.topAge(), this.getAge(self));
        BlockState above = level.getBlockState(pos.above());
        if (above.is(this.topBlock())) {
            if (above.getValue(this.topAge()) != this.getAge(self)) {
                level.setBlock(pos.above(), want, 3);
            }
        } else if (above.isAir()) {
            level.setBlock(pos.above(), want, 3);
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (this.shapes == null) {
            int maxAge = this.getMaxAge();
            this.shapes = Block.boxes(maxAge + 1, age -> Block.column(16.0, 0.0, Math.min(16, 2 + age * 2)));
        }
        return this.shapes[this.getAge(state)];
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(this.getAgeProperty());
    }
}
