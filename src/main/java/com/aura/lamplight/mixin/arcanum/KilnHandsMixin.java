package com.aura.lamplight.mixin.arcanum;

import com.aura.lamplight.arcanum.server.RebuildHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class KilnHandsMixin {

    @Inject(method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At("HEAD"), cancellable = true)
    private static void lamplight$toolDropOverrides(BlockState state, Level level, BlockPos pos, BlockEntity blockEntity,
                                                    Entity entity, ItemStack tool, CallbackInfo ci) {
        if (level instanceof ServerLevel serverLevel && entity instanceof Player player) {
            RebuildHandler.onBlockDrop(serverLevel, player, pos, state, tool, ci);
        }
    }

    @Inject(method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At("TAIL"))
    private static void lamplight$extraToolDrops(BlockState state, Level level, BlockPos pos, BlockEntity blockEntity,
                                                 Entity entity, ItemStack tool, CallbackInfo ci) {
        if (level instanceof ServerLevel serverLevel && entity instanceof Player player) {
            RebuildHandler.afterBlockDrop(serverLevel, player, pos, state, tool, blockEntity, entity);
        }
    }
}
