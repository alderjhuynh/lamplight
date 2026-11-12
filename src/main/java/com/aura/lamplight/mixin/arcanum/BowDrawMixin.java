package com.aura.lamplight.mixin.arcanum;

import com.aura.lamplight.arcanum.enchantment.ArcanumEnchantments;
import com.aura.lamplight.arcanum.server.RebuildHandler;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BowItem.class)
public class BowDrawMixin {

    @Unique
    private static final ThreadLocal<ItemStack> LAMPLIGHT$BOW = new ThreadLocal<>();

    @Inject(method = "releaseUsing", at = @At("HEAD"))
    private void lamplight$heavyDrawCapture(ItemStack stack, Level level, LivingEntity entity, int timeLeft,
                                            CallbackInfoReturnable<Boolean> cir) {
        LAMPLIGHT$BOW.set(stack);
    }

    @ModifyArg(method = "releaseUsing",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BowItem;getPowerForTime(I)F"))
    private int lamplight$heavyDrawSlowerDraw(int ticksUsed) {
        ItemStack stack = LAMPLIGHT$BOW.get();
        int lvl = RebuildHandler.lvl(stack, ArcanumEnchantments.HEAVY_DRAW);
        if (lvl <= 0) return ticksUsed;
        return Math.round(ticksUsed / (1.0F + 0.5F * lvl));
    }

    @Inject(method = "releaseUsing", at = @At("RETURN"))
    private void lamplight$heavyDrawRelease(ItemStack stack, Level level, LivingEntity entity, int timeLeft,
                                            CallbackInfoReturnable<Boolean> cir) {
        LAMPLIGHT$BOW.remove();
    }
}
