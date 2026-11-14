package com.aura.lamplight.ambrosia.item;

import com.aura.lamplight.ambrosia.block.CampfireCookingPotBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class CookingPotItem extends BlockItem {
    private final Block campfirePot;
    private final Block soulCampfirePot;

    public CookingPotItem(Block groundPot, Block campfirePot, Block soulCampfirePot, Properties properties) {
        super(groundPot, properties);
        this.campfirePot = campfirePot;
        this.soulCampfirePot = soulCampfirePot;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState campfire = level.getBlockState(pos);
        if (!(campfire.getBlock() instanceof CampfireBlock)) {
            return super.useOn(context);
        }
        Block pot = campfire.is(Blocks.SOUL_CAMPFIRE) ? this.soulCampfirePot : this.campfirePot;
        BlockState potState = pot.defaultBlockState()
                .setValue(CampfireCookingPotBlock.FACING, campfire.getValue(CampfireBlock.FACING))
                .setValue(CampfireCookingPotBlock.LIT, campfire.getValue(CampfireBlock.LIT));
        level.setBlock(pos, potState, Block.UPDATE_ALL);
        level.playSound(context.getPlayer(), pos, potState.getSoundType().getPlaceSound(),
                SoundSource.BLOCKS, 0.5F, 1.0F);
        Player player = context.getPlayer();
        if (player == null || !player.hasInfiniteMaterials()) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.SUCCESS;
    }
}
