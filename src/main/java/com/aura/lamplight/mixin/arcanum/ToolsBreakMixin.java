package com.aura.lamplight.mixin.arcanum;

import com.aura.lamplight.arcanum.server.RebuildHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public class ToolsBreakMixin {

    @Shadow
    @Final
    protected ServerPlayer player;

    @Inject(method = "destroyBlock", at = @At("HEAD"))
    private void lamplight$rebuildBreakHead(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        RebuildHandler.onBlockBreak(this.player, pos);
    }

    @Inject(method = "destroyBlock", at = @At("TAIL"))
    private void lamplight$rebuildBreakTail(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        RebuildHandler.afterBlockBreak(this.player, pos, cir.getReturnValueZ());
    }
}
