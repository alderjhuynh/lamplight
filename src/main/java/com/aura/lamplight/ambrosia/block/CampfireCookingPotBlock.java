package com.aura.lamplight.ambrosia.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import com.aura.lamplight.ambrosia.AmbrosiaSounds;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.stats.Stats;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CampfireCookingPotBlock extends CookingPotBlock implements EntityBlock {
    public static final MapCodec<CampfireCookingPotBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.fieldOf("soul_campfire").forGetter(pot -> pot.soulCampfire),
            BlockBehaviour.propertiesCodec()).apply(instance, CampfireCookingPotBlock::new));
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final EnumProperty<PotFluid> FLUID = EnumProperty.create("fluid", PotFluid.class);

    private static final VoxelShape SHAPE_NS = Shapes.or(
            Block.box(1.0, 0.0, 0.0, 5.0, 4.0, 16.0),
            Block.box(11.0, 0.0, 0.0, 15.0, 4.0, 16.0),
            Block.box(0.0, 3.0, 1.0, 16.0, 7.0, 5.0),
            Block.box(0.0, 3.0, 11.0, 16.0, 7.0, 15.0),
            Block.box(1.0, 7.0, 1.0, 15.0, 16.0, 15.0));

    private static final VoxelShape SHAPE_EW = Shapes.or(
            Block.box(0.0, 0.0, 1.0, 16.0, 4.0, 5.0),
            Block.box(0.0, 0.0, 11.0, 16.0, 4.0, 15.0),
            Block.box(1.0, 3.0, 0.0, 5.0, 7.0, 16.0),
            Block.box(11.0, 3.0, 0.0, 15.0, 7.0, 16.0),
            Block.box(1.0, 7.0, 1.0, 15.0, 16.0, 15.0));

    private final boolean soulCampfire;

    public CampfireCookingPotBlock(boolean soulCampfire, BlockBehaviour.Properties properties) {
        super(properties);
        this.soulCampfire = soulCampfire;
        this.registerDefaultState(this.defaultBlockState().setValue(LIT, false).setValue(FLUID, PotFluid.EMPTY));
    }

    @Override
    public MapCodec<? extends CookingPotBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIT, FLUID);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(FACING).getAxis() == Direction.Axis.X ? SHAPE_EW : SHAPE_NS;
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult
    ) {
        if (stack.isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        PotFluid fluid = state.getValue(FLUID);
        if (fluid == PotFluid.EMPTY) {
            PotFluid pour = PotFluid.pourOf(stack);
            if (pour == null) {
                return InteractionResult.PASS;
            }
            if (!state.getValue(LIT)) {
                return InteractionResult.FAIL;
            }
            if (!level.isClientSide()) {
                level.setBlock(pos, state.setValue(FLUID, pour), Block.UPDATE_ALL);
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, PotFluid.pourRemainder(stack)));
                player.awardStat(Stats.FILL_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                level.playSound(null, pos, AmbrosiaSounds.COOKING_POT_INSERT, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
            }
            return InteractionResult.SUCCESS;
        }
        CookingPotBlockEntity pot = level.getBlockEntity(pos) instanceof CookingPotBlockEntity blockEntity ? blockEntity : null;
        if (stack.is(Items.GLASS_BOTTLE)) {
            if (pot != null && !pot.isEmpty()) {
                return InteractionResult.FAIL;
            }
            if (!level.isClientSide()) {
                level.setBlock(pos, state.setValue(FLUID, PotFluid.EMPTY), Block.UPDATE_ALL);
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, fluid.scoopItem()));
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(Items.GLASS_BOTTLE));
                level.playSound(null, pos, AmbrosiaSounds.COOKING_POT_COLLECT, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
            }
            return InteractionResult.SUCCESS;
        }
        if (PotFluid.pourOf(stack) != null) {
            return InteractionResult.FAIL;
        }
        if (pot != null && pot.canAdd(stack)) {
            if (!level.isClientSide()) {
                pot.addIngredient(stack);
                player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                stack.consume(1, player); // respects creative mode
                level.playSound(null, pos, AmbrosiaSounds.COOKING_POT_INSERT, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof CookingPotBlockEntity pot && !pot.isEmpty()) {
            if (!level.isClientSide()) {
                ItemStack popped = pot.popIngredient();
                if (!player.getInventory().add(popped)) {
                    player.drop(popped, false);
                }
                level.playSound(null, pos, AmbrosiaSounds.COOKING_POT_COLLECT, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CookingPotBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null
                : createTickerHelper(type, ModBlockEntities.COOKING_POT, CookingPotBlockEntity::tick);
    }

    private static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(
            BlockEntityType<A> givenType, BlockEntityType<E> expectedType, BlockEntityTicker<? super E> ticker) {
        return givenType == expectedType ? (BlockEntityTicker<A>) ticker : null;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT) || state.getValue(FLUID) == PotFluid.EMPTY) {
            return;
        }
        if (random.nextFloat() < 0.35F) {
            double x = pos.getX() + 0.1875D + random.nextDouble() * 0.625D;
            double z = pos.getZ() + 0.1875D + random.nextDouble() * 0.625D;
            double y = pos.getY() + 0.875D;
            level.addParticle(ColorParticleOption.create(PotParticles.COOKING_POT_BUBBLE, state.getValue(FLUID).color()), x, y, z, 0.0D, 0.005D, 0.0D);
            if (random.nextFloat() < 0.4F) {
                level.addParticle(ColorParticleOption.create(PotParticles.COOKING_POT_BUBBLE, state.getValue(FLUID).color()),
                        x + (random.nextDouble() - 0.5D) * 0.1D, y, z + (random.nextDouble() - 0.5D) * 0.1D, 0.0D, 0.005D, 0.0D);
            }
        }
        if (random.nextFloat() < 0.02F) {
            level.playLocalSound(pos, AmbrosiaSounds.COOKING_POT_AMBIENT, SoundSource.BLOCKS, 1.0F, 1.0F, false);
        }
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
        if (!level.isClientSide() && level.getBlockState(pos).isAir()) {
            BlockState campfire = (this.soulCampfire ? Blocks.SOUL_CAMPFIRE : Blocks.CAMPFIRE).defaultBlockState()
                    .setValue(CampfireBlock.FACING, state.getValue(FACING))
                    .setValue(CampfireBlock.LIT, state.getValue(LIT));
            level.setBlock(pos, campfire, Block.UPDATE_ALL);
        }
    }
}