package com.aura.lamplight.mixin.arcanum;

import com.aura.lamplight.arcanum.server.RebuildHandler;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BoneMealItem.class)
public class BoneMealMixin {

    @Inject(method = "useOn", at = @At("HEAD"))
    private void lamplight$tillerBoneMealHead(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        RebuildHandler.boneMealHead(context.getItemInHand());
    }

    @Inject(method = "useOn", at = @At("RETURN"))
    private void lamplight$tillerBoneMealTail(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        RebuildHandler.boneMealTail(context.getItemInHand());
    }
}
