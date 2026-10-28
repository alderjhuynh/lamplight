package com.aura.lamplight.wayfarer.waystone;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.BlockHitResult;

public class WaystoneBlock extends Block {

    public static final MapCodec<WaystoneBlock> CODEC = BlockBehaviour.simpleCodec(WaystoneBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public WaystoneBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(LIT, Boolean.FALSE));
    }

    @Override
    public MapCodec<? extends WaystoneBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            placeAt(level, state, pos, Block.UPDATE_ALL);
        } else {
            super.setPlacedBy(level, pos, state, placer, stack);
        }
    }

    public static void placeAt(LevelAccessor level, BlockState state, BlockPos pos, int flags) {
        BlockState lower = state.setValue(HALF, DoubleBlockHalf.LOWER);
        level.setBlock(pos, lower, flags);
        level.setBlock(pos.above(), lower.setValue(HALF, DoubleBlockHalf.UPPER), flags);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockState below = level.getBlockState(pos.below());
            return below.is(this) && below.getValue(HALF) == DoubleBlockHalf.LOWER;
        }
        BlockState below = level.getBlockState(pos.below());
        return below.isFaceSturdy(level, pos.below(), Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess access,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        if (isTouchingOtherHalf(state, direction) && (!neighborState.is(this) || neighborState.getValue(HALF) == state.getValue(HALF))) {
            return Blocks.AIR.defaultBlockState();
        }
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER && direction == Direction.DOWN && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, access, pos, direction, neighborPos, neighborState, random);
    }

    private static boolean isTouchingOtherHalf(BlockState state, Direction direction) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? direction == Direction.UP : direction == Direction.DOWN;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && player.isCreative()) {
            BlockPos otherPos = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
            BlockState other = level.getBlockState(otherPos);
            if (other.is(this) && other.getValue(HALF) != state.getValue(HALF)) {
                level.setBlock(otherPos, Blocks.AIR.defaultBlockState(),
                        Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF, LIT);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer traveler && level instanceof ServerLevel serverLevel) {
            BlockPos base = base(state, pos);
            BlockState baseState = serverLevel.getBlockState(base);
            if (baseState.is(ModBlocks.WAYSTONE)) {
                ResourceKey<Level> dimension = serverLevel.dimension();
                WaystoneNetwork network = WaystoneNetwork.get(serverLevel);
                if (!baseState.getValue(LIT)) {
                    kindle(serverLevel, base, baseState);
                    network.add(dimension, base);
                    ServerPlayNetworking.send(traveler, new WaystoneNamingPayload(base, ""));
                    return InteractionResult.CONSUME;
                }
                network.rememberIfMissing(dimension, base);
                if (player.isShiftKeyDown()) {
                    ServerPlayNetworking.send(traveler, new WaystoneNamingPayload(
                            base, network.name(dimension, base)));
                } else if (!WaystoneChanneling.isChanneling(traveler)) {
                    ServerPlayNetworking.send(traveler, new WaystoneTravelListPayload(
                            base, network.travelStones(traveler, base, dimension)));
                }
            }
        }
        return InteractionResult.CONSUME;
    }

    private static void kindle(ServerLevel level, BlockPos base, BlockState baseState) {
        level.setBlock(base, baseState.setValue(LIT, Boolean.TRUE), Block.UPDATE_ALL);
        BlockState upper = level.getBlockState(base.above());
        if (upper.is(ModBlocks.WAYSTONE) && upper.getValue(HALF) == DoubleBlockHalf.UPPER) {
            level.setBlock(base.above(), upper.setValue(LIT, Boolean.TRUE), Block.UPDATE_ALL);
        }
        level.playSound(null, base, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 0.8F, 1.0F);
        level.sendParticles(ParticleTypes.END_ROD,
                base.getX() + 0.5, base.getY() + 2.1, base.getZ() + 0.5,
                10, 0.25, 0.5, 0.25, 0.01);
    }

    public static BlockPos base(BlockState state, BlockPos pos) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
    }

    public static boolean isLitLower(BlockState state) {
        return state.is(ModBlocks.WAYSTONE)
                && state.getValue(HALF) == DoubleBlockHalf.LOWER
                && state.getValue(LIT);
    }

    public static Direction facing(ServerLevel level, BlockPos base) {
        return level.getBlockState(base).getValue(FACING);
    }
}
