package com.aura.ambrosia.block;

import com.aura.ambrosia.item.ModItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CuttingBoardBlock extends Block {
    public static final MapCodec<CuttingBoardBlock> CODEC = BlockBehaviour.simpleCodec(CuttingBoardBlock::new);
    private static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 2.0, 15.0);

    public CuttingBoardBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends CuttingBoardBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack knife, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND || !ModItems.isKnife(knife)) {
            return InteractionResult.PASS;
        }
        ItemStack offhand = player.getOffhandItem();
        Item cutlet = cutletFor(offhand.getItem());
        if (cutlet == null) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!player.hasInfiniteMaterials()) {
            offhand.shrink(1);
            knife.hurtAndBreak(1, player, hand);
        }
        ItemStack result = new ItemStack(cutlet, 2);
        if (player.getOffhandItem().isEmpty()) {
            player.setItemInHand(InteractionHand.OFF_HAND, result);
        } else if (!player.getInventory().add(result)) {
            player.drop(result, false);
        }
        player.getCooldowns().addCooldown(knife, 10);
        level.playSound(null, pos, net.minecraft.world.level.block.SoundType.WOOD.getHitSound(), SoundSource.BLOCKS, 0.8F, 1.2F);
        return InteractionResult.CONSUME;
    }

    /** Raw meat/fish to its cutlet, or null if it cannot be broken down. */
    public static Item cutletFor(Item item) {
        if (item == Items.BEEF) {
            return ModItems.BEEF_CUTLETS;
        } else if (item == Items.CHICKEN) {
            return ModItems.CHICKEN_CUTLETS;
        } else if (item == Items.MUTTON) {
            return ModItems.MUTTON_CUTLETS;
        } else if (item == Items.RABBIT) {
            return ModItems.RABBIT_CUTLETS;
        } else if (item == Items.COD) {
            return ModItems.COD_FILLETS;
        } else if (item == Items.SALMON) {
            return ModItems.SALMON_FILLETS;
        }
        return null;
    }
}
