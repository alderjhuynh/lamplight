package com.aura.lamplight.client.mixin.ambrosia;

import com.aura.lamplight.ambrosia.food.PanFriedFoods;
import com.aura.lamplight.ambrosia.item.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Third-person counterpart: hides the offhand food while pan-frying and draws
 * it lying inside the frying pan instead.
 */
@Mixin(ItemInHandLayer.class)
public abstract class FryingPanThirdPersonMixin {
    @Inject(method = "submitArmWithItem", at = @At("HEAD"), cancellable = true)
    private void ambrosia$hideOffhandFood(ArmedEntityRenderState state, ItemStackRenderState itemState,
                                          ItemStack stack, HumanoidArm arm, PoseStack poses,
                                          SubmitNodeCollector collector, int light, CallbackInfo ci) {
        if (stack.is(ModItems.FRYING_PAN) || state == null) {
            return;
        }
        if (!PanFriedFoods.isUncookedFoodClient(stack)) {
            return;
        }
        // If the other hand holds the pan and is actively using it, the food
        // lives in the pan — don't also draw it in the empty hand.
        ItemStack other = arm == HumanoidArm.RIGHT ? state.leftHandItemStack : state.rightHandItemStack;
        if (other != null && other.is(ModItems.FRYING_PAN) && state.ticksUsingItem(otherArm(arm)) > 0.0F) {
            ci.cancel();
        }
    }

    @Inject(method = "submitArmWithItem",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V",
                    shift = At.Shift.AFTER))
    private void ambrosia$drawFoodInPan(ArmedEntityRenderState state, ItemStackRenderState itemState,
                                        ItemStack stack, HumanoidArm arm, PoseStack poses,
                                        SubmitNodeCollector collector, int light, CallbackInfo ci) {
        if (state == null || !stack.is(ModItems.FRYING_PAN)) {
            return;
        }
        if (state.ticksUsingItem(arm) <= 0.0F) {
            return;
        }
        ItemStackRenderState foodState;
        ItemStack foodStack;
        if (arm == HumanoidArm.RIGHT) {
            foodState = state.leftHandItemState;
            foodStack = state.leftHandItemStack;
        } else {
            foodState = state.rightHandItemState;
            foodStack = state.rightHandItemStack;
        }
        if (foodState == null || foodState.isEmpty() || foodStack == null
                || !PanFriedFoods.isUncookedFoodClient(foodStack)) {
            return;
        }
        poses.pushPose();
        try {
            // Lay the food flat-ish inside the pan bowl.
            poses.translate(0.0F, 0.12F, 0.0F);
            poses.scale(0.55F, 0.55F, 0.55F);
            foodState.submit(poses, collector, light, OverlayTexture.NO_OVERLAY, state.outlineColor);
        } finally {
            poses.popPose();
        }
    }

    @Unique
    private static HumanoidArm otherArm(HumanoidArm arm) {
        return arm == HumanoidArm.RIGHT ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
    }
}
