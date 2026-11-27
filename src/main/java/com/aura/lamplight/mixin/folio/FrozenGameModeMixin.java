package com.aura.lamplight.mixin.folio;

import com.aura.lamplight.folio.effect.Frozen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public abstract class FrozenGameModeMixin {

    @Shadow
    @Final
    protected ServerPlayer player;

    @Inject(method = "useItem", at = @At("HEAD"), cancellable = true)
    private void folio$frozenUseItem(
        final ServerPlayer user,
        final Level level,
        final ItemStack itemStack,
        final InteractionHand hand,
        final CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (Frozen.is(user)) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void folio$frozenUseItemOn(
        final ServerPlayer user,
        final Level level,
        final ItemStack itemStack,
        final InteractionHand hand,
        final BlockHitResult hitResult,
        final CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (Frozen.is(user)) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }

    @Inject(method = "handleBlockBreakAction", at = @At("HEAD"), cancellable = true)
    private void folio$frozenDig(
        final BlockPos pos,
        final ServerboundPlayerActionPacket.Action action,
        final Direction direction,
        final int maxY,
        final int sequence,
        final CallbackInfo ci
    ) {
        if (Frozen.is(this.player)) {
            ci.cancel();
        }
    }
}
