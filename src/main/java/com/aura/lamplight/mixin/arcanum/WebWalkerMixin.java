package com.aura.lamplight.mixin.arcanum;

import com.aura.lamplight.arcanum.enchantment.ArcanumEnchantments;
import com.aura.lamplight.arcanum.server.RebuildHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WebBlock.class)
public class WebWalkerMixin {

    @Inject(method = "entityInside", at = @At("HEAD"), cancellable = true)
    private void lamplight$webWalker(BlockState state, Level level, BlockPos pos, Entity entity,
                                     InsideBlockEffectApplier applier, boolean moved, CallbackInfo ci) {
        if (entity instanceof Player player
                && RebuildHandler.armorLvl(player, ArcanumEnchantments.WEB_WALKER) > 0) {
            ci.cancel();
        }
    }
}
