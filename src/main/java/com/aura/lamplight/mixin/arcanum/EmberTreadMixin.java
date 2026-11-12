package com.aura.lamplight.mixin.arcanum;

import com.aura.lamplight.arcanum.enchantment.ArcanumEnchantments;
import com.aura.lamplight.arcanum.server.RebuildHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.MagmaBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MagmaBlock.class)
public class EmberTreadMixin {

    @Inject(method = "stepOn", at = @At("HEAD"), cancellable = true)
    private void lamplight$emberTread(Level level, BlockPos pos, BlockState state, Entity entity, CallbackInfo ci) {
        if (entity instanceof Player player
                && RebuildHandler.armorLvl(player, ArcanumEnchantments.EMBER_TREAD) > 0) {
            ci.cancel();
        }
    }
}
