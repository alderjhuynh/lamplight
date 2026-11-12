package com.aura.lamplight.mixin.arcanum;

import com.aura.lamplight.arcanum.enchantment.ArcanumEnchantments;
import com.aura.lamplight.arcanum.server.RebuildHandler;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrossbowItem.class)
public class CrossbowDrawMixin {

    @Inject(method = "getChargeDuration", at = @At("RETURN"), cancellable = true)
    private static void lamplight$heavyDrawLongerCharge(ItemStack stack, LivingEntity entity,
                                                        CallbackInfoReturnable<Integer> cir) {
        int lvl = RebuildHandler.lvl(stack, ArcanumEnchantments.HEAVY_DRAW);
        if (lvl > 0) {
            cir.setReturnValue(Math.round(cir.getReturnValueI() * (1.0F + 0.5F * lvl)));
        }
    }
}
