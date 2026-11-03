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
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WaystoneBlock extends BaseEntityBlock {

    public static final MapCodec<WaystoneBlock> CODEC = BlockBehaviour.simpleCodec(WaystoneBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    private static final VoxelShape SHAPE = net.minecraft.world.phys.shapes.Shapes.box(
            6 / 16.0, 0.0, 6 / 16.0, 10 / 16.0, 7 / 16.0, 10 / 16.0);

    public WaystoneBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
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
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return below.isFaceSturdy(level, pos.below(), Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess access,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        if (direction == Direction.DOWN && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, access, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WaystoneBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer traveler && level instanceof ServerLevel serverLevel) {
            BlockState stoneState = serverLevel.getBlockState(pos);
            if (stoneState.is(ModBlocks.WAYSTONE)) {
                ResourceKey<Level> dimension = serverLevel.dimension();
                WaystoneNetwork network = WaystoneNetwork.get(serverLevel);
                if (!stoneState.getValue(LIT)) {
                    kindle(serverLevel, pos, stoneState);
                    network.add(dimension, pos);
                    ServerPlayNetworking.send(traveler, new WaystoneNamingPayload(pos, ""));
                    return InteractionResult.CONSUME;
                }
                network.rememberIfMissing(dimension, pos);
                if (player.isShiftKeyDown()) {
                    ServerPlayNetworking.send(traveler, new WaystoneNamingPayload(
                            pos, network.name(dimension, pos)));
                } else if (!WaystoneChanneling.isChanneling(traveler)) {
                    ServerPlayNetworking.send(traveler, new WaystoneTravelListPayload(
                            pos, network.travelStones(traveler, pos, dimension)));
                }
            }
        }
        return InteractionResult.CONSUME;
    }

    private static void kindle(ServerLevel level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state.setValue(LIT, Boolean.TRUE), Block.UPDATE_ALL);
        level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 0.8F, 1.0F);
        level.sendParticles(ModParticles.WAYSTONE_TELEPORT,
                pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5,
                10, 0.25, 0.3, 0.25, 0.01);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) {
            return;
        }
        double particleX = pos.getX() + 0.5;
        double particleY = pos.getY() + 0.6;
        double particleZ = pos.getZ() + 0.5;
        if (random.nextDouble() < 0.5) {
            level.addParticle(ParticleTypes.SMOKE,
                    particleX + (random.nextDouble() - 0.5) * 0.06, particleY,
                    particleZ + (random.nextDouble() - 0.5) * 0.06,
                    0.0, 0.0, 0.0);
        }
        level.addParticle(ParticleTypes.FLAME, particleX, particleY, particleZ, 0.0, 0.0, 0.0);
    }

    public static boolean isLit(BlockState state) {
        return state.is(ModBlocks.WAYSTONE)
                && state.getValue(LIT);
    }

    public static Direction facing(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).getValue(FACING);
    }
}
