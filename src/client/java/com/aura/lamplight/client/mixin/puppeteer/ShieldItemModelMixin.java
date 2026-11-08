package com.aura.lamplight.client.mixin.puppeteer;

import com.aura.lamplight.puppeteer.client.AnimationClientState;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.IsUsingItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IsUsingItem.class)
public abstract class ShieldItemModelMixin {

    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    private void puppeteer$cancelShieldBlockModel(final ItemStack stack, final ClientLevel level,
        final @Nullable LivingEntity entity, final int seed, final ItemDisplayContext context,
        final CallbackInfoReturnable<Boolean> cir) {
        if (entity == null || !(stack.getItem() instanceof ShieldItem) || context.firstPerson()) {
            return;
        }
        AnimationClientState.ActiveSet activeSet = AnimationClientState.get(entity.getId());
        if (activeSet == null) {
            return;
        }
        for (AnimationClientState.Layer layer : activeSet.layers()) {
            if (layer.animationId().getPath().startsWith("shield_")) {
                cir.setReturnValue(false);
                return;
            }
        }
    }
}
