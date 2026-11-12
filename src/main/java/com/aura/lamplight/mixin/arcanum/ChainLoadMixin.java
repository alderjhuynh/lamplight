package com.aura.lamplight.mixin.arcanum;

import com.aura.lamplight.arcanum.server.RebuildHandler;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrossbowItem.class)
public class ChainLoadMixin {

    @Inject(method = "use", at = @At("HEAD"))
    private void lamplight$chainLoadCycleReset(Level level, Player player, InteractionHand hand,
                                               CallbackInfoReturnable<net.minecraft.world.InteractionResult> cir) {
        RebuildHandler.onCrossbowUse(player, player.getItemInHand(hand));
    }

    @Inject(method = "performShooting", at = @At("HEAD"))
    private void lamplight$chainLoadSnapshot(Level level, LivingEntity entity, InteractionHand hand,
                                             ItemStack stack, float power, float spread, LivingEntity target,
                                             CallbackInfo ci) {
        if (entity instanceof Player player) {
            RebuildHandler.onCrossbowReleaseHead(player, stack);
        }
    }

    @Inject(method = "performShooting", at = @At("TAIL"))
    private void lamplight$chainLoadReArm(Level level, LivingEntity entity, InteractionHand hand,
                                          ItemStack stack, float power, float spread, LivingEntity target,
                                          CallbackInfo ci) {
        if (entity instanceof Player player) {
            RebuildHandler.onCrossbowReleaseTail(player, stack);
        }
    }
}
