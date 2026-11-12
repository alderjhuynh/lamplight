package com.aura.lamplight.mixin.arcanum;

import com.aura.lamplight.arcanum.server.RebuildHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
public abstract class RangedMixin {

    @Inject(method = "onHitBlock", at = @At("TAIL"))
    private void lamplight$rebuildArrowHitBlock(BlockHitResult result, CallbackInfo ci) {
        AbstractArrow arrow = (AbstractArrow) (Object) this;
        if (arrow.level() instanceof ServerLevel level) {
            RebuildHandler.onArrowHitBlock(level, arrow, result.getBlockPos());
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void lamplight$rebuildArrowTick(CallbackInfo ci) {
        AbstractArrow arrow = (AbstractArrow) (Object) this;
        if (arrow.tickCount == 1 && arrow.level() instanceof ServerLevel level) {
            RebuildHandler.onArrowLaunch(level, arrow);
        }
    }
}
