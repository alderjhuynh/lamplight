package com.aura.lamplight.mixin.arcanum;

import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ThrownTrident.class)
public class TridentLoyaltyMixin {

    @Inject(method = "getLoyaltyFromItem", at = @At("HEAD"), cancellable = true, remap = false)
    private void arcanum$alwaysLoyalty(ItemStack stack, CallbackInfoReturnable<Byte> cir) {
        cir.setReturnValue((byte) 1);
    }
}
