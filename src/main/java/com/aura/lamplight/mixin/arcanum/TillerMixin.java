package com.aura.lamplight.mixin.arcanum;

import com.aura.lamplight.arcanum.enchantment.ArcanumEnchantments;
import com.aura.lamplight.arcanum.server.RebuildHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FarmlandBlock.class)
public class TillerMixin {

    @Inject(method = "fallOn", at = @At("HEAD"), cancellable = true)
    private void lamplight$tillerNoTrample(Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance,
                                           CallbackInfo ci) {
        if (entity instanceof Player player
                && RebuildHandler.lvl(player.getMainHandItem(), ArcanumEnchantments.TILLER) > 0) {
            ci.cancel();
        }
    }
}
