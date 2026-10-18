package com.aura.lamplight.panacea.block;

import com.aura.lamplight.panacea.blockentity.PotionCauldronBlockEntity;
import com.aura.lamplight.panacea.item.TippedWeapon;
import com.aura.lamplight.panacea.registry.ModComponents;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class PotionCauldronBlock extends BaseEntityBlock {
    public static final MapCodec<PotionCauldronBlock> CODEC = simpleCodec(PotionCauldronBlock::new);
    public static final int MAX_FILL_LEVEL = 3;
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, MAX_FILL_LEVEL);

    public PotionCauldronBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, 0));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PotionCauldronBlockEntity(pos, state);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        return state.getValue(LEVEL);
    }

    private boolean isFull(BlockState state) {
        return state.getValue(LEVEL) >= MAX_FILL_LEVEL;
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
        if (!(level.getBlockEntity(pos) instanceof PotionCauldronBlockEntity cauldron)) {
            return InteractionResult.PASS;
        }

        if (stack.getItem() instanceof net.minecraft.world.item.PotionItem) {
            return this.fillFromPotion(stack, state, level, pos, player, hand, cauldron);
        }
        if (stack.is(Items.GLASS_BOTTLE)) {
            return this.takeTopLayer(stack, state, level, pos, player, hand, cauldron);
        }
        if (stack.is(Items.ARROW)) {
            return this.tipArrows(stack, state, level, pos, player, hand, cauldron);
        }
        if (stack.is(ItemTags.SWORDS)) {
            return this.tipSword(stack, state, level, pos, player, hand, cauldron);
        }
        return InteractionResult.PASS;
    }

    private InteractionResult fillFromPotion(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            PotionCauldronBlockEntity cauldron
    ) {
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null || !contents.hasEffects() || contents.potion().isEmpty() || this.isFull(state)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        if (!level.isClientSide()) {
            Item usedItem = stack.getItem();
            cauldron.addLayer(contents);
            level.setBlockAndUpdate(pos, state.setValue(LEVEL, state.getValue(LEVEL) + 1));
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
            player.awardStat(Stats.FILL_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(usedItem));
            level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
        }
        return InteractionResult.SUCCESS;
    }

    private InteractionResult takeTopLayer(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            PotionCauldronBlockEntity cauldron
    ) {
        if (cauldron.isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        if (!level.isClientSide()) {
            Item usedItem = stack.getItem();
            cauldron.removeTopLayer().ifPresent(contents -> {
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, toPotionStack(contents)));
                lowerFillLevel(state, level, pos);
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(usedItem));
                level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
            });
        }
        return InteractionResult.SUCCESS;
    }

    private InteractionResult tipArrows(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            PotionCauldronBlockEntity cauldron
    ) {
        if (cauldron.isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        if (!level.isClientSide()) {
            Item usedItem = stack.getItem();
            PotionContents merged = PotionCauldronBlockEntity.merge(cauldron.getLayers());
            ItemStack tipped = PotionCauldronBlockEntity.mixedArrowStack(merged);
            tipped.setCount(stack.getCount());
            player.setItemInHand(hand, tipped);
            lowerFillLevel(state, level, pos);
            player.awardStat(Stats.USE_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(usedItem));
            level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
        }
        return InteractionResult.SUCCESS;
    }

    private InteractionResult tipSword(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            PotionCauldronBlockEntity cauldron
    ) {
        if (cauldron.isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        if (!level.isClientSide()) {
            Item usedItem = stack.getItem();
            PotionContents merged = PotionCauldronBlockEntity.merge(cauldron.getLayers());
            stack.set(ModComponents.TIPPED_WEAPON, TippedWeapon.of(merged));
            stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
            cauldron.clearLayers();
            level.setBlockAndUpdate(pos, state.setValue(LEVEL, 0));
            player.awardStat(Stats.USE_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(usedItem));
            level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
        }
        return InteractionResult.SUCCESS;
    }

    private static void lowerFillLevel(BlockState state, Level level, BlockPos pos) {
        int newLevel = Math.max(0, state.getValue(LEVEL) - 1);
        level.setBlockAndUpdate(pos, state.setValue(LEVEL, newLevel));
    }

    static ItemStack toPotionStack(PotionContents contents) {
        ItemStack potionStack = new ItemStack(Items.POTION);
        potionStack.set(DataComponents.POTION_CONTENTS, contents);
        return potionStack;
    }
}
