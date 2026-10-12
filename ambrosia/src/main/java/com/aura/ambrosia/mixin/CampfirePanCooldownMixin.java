package com.aura.ambrosia.mixin;

import com.aura.ambrosia.item.FryingPanItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CampfireBlock.class)
public abstract class CampfirePanCooldownMixin {

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void ambrosia$blockOffhandPlaceDuringPanCooldown(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit,
            CallbackInfoReturnable<InteractionResult> cir) {
        if (hand != InteractionHand.OFF_HAND) {
            return;
        }
        if (player == null || stack.isEmpty()) {
            return;
        }
        ItemStack mainhand = player.getMainHandItem();
        if (!(mainhand.getItem() instanceof FryingPanItem)) {
            return;
        }
        if (!player.getCooldowns().isOnCooldown(mainhand)) {
            return;
        }
        cir.setReturnValue(InteractionResult.FAIL);
    }
}
