package com.aura.lamplight.panacea.block;

import com.aura.lamplight.panacea.blockentity.PotionCauldronBlockEntity;
import com.aura.lamplight.panacea.registry.ModBlocks;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Handles interactions that vanilla's 26.x interaction routing no longer reaches
 * through {@code BlockBehaviour#useItemOn}:
 *
 * <ul>
 *   <li>Sneak + glass bottle on a filled potion cauldron bottles all layers into a
 *       mixed potion (sneaking bypasses block item-interaction entirely in 26.x).</li>
 *   <li>Potions used on a plain empty cauldron, converting it into a potion cauldron.</li>
 * </ul>
 */
public final class PotionCauldronInteractions {
    private PotionCauldronInteractions() {
    }

    public static void register() {
        UseBlockCallback.EVENT.register(PotionCauldronInteractions::onUseBlock);
    }

    private static InteractionResult onUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if (player.isSpectator()) {
            return InteractionResult.PASS;
        }

        ItemStack stack = player.getItemInHand(hand);
        BlockPos pos = hitResult.getBlockPos();
        BlockState state = level.getBlockState(pos);

        if (player.isShiftKeyDown()
                && stack.is(Items.GLASS_BOTTLE)
                && state.is(ModBlocks.POTION_CAULDRON)
                && level.getBlockEntity(pos) instanceof PotionCauldronBlockEntity cauldron
                && !cauldron.isEmpty()) {
            return mixIntoBottle(stack, state, level, pos, player, hand, cauldron);
        }

        if (state.is(Blocks.CAULDRON)
                && stack.getItem() instanceof PotionItem
                && stack.get(DataComponents.POTION_CONTENTS) instanceof PotionContents contents
                && contents.hasEffects()
                && contents.potion().isPresent()) {
            return startFromVanillaCauldron(contents, level, pos, player, hand, stack);
        }

        return InteractionResult.PASS;
    }

    private static InteractionResult mixIntoBottle(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            PotionCauldronBlockEntity cauldron
    ) {
        if (!level.isClientSide()) {
            Item usedItem = stack.getItem();
            PotionContents merged = PotionCauldronBlockEntity.merge(cauldron.getLayers());
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, PotionCauldronBlockEntity.mixedPotionStack(merged)));
            cauldron.clearLayers();
            level.setBlockAndUpdate(pos, state.setValue(PotionCauldronBlock.LEVEL, 0));
            player.awardStat(Stats.USE_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(usedItem));
            level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
        }
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult startFromVanillaCauldron(
            PotionContents contents,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            ItemStack stack
    ) {
        if (!level.isClientSide()) {
            Item usedItem = stack.getItem();
            level.setBlockAndUpdate(pos, ModBlocks.POTION_CAULDRON.defaultBlockState().setValue(PotionCauldronBlock.LEVEL, 1));
            if (level.getBlockEntity(pos) instanceof PotionCauldronBlockEntity cauldron) {
                cauldron.addLayer(contents);
            }
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
            player.awardStat(Stats.FILL_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(usedItem));
            level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
        }
        return InteractionResult.SUCCESS;
    }
}
