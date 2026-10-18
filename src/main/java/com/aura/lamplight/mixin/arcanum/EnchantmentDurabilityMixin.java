package com.aura.lamplight.mixin.arcanum;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public class EnchantmentDurabilityMixin {
    @Inject(method = "processDurabilityChange", at = @At("HEAD"), cancellable = true)
    private static void arcanum$noDurabilityChange(ServerLevel level, ItemStack stack, int durability, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(0);
    }

    @Inject(method = "modifyDurabilityToRepairFromXp", at = @At("HEAD"), cancellable = true)
    private static void arcanum$noRepair(ServerLevel level, ItemStack stack, int i, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(0);
    }
}
