package com.aura.ambrosia.client.mixin;

import com.aura.ambrosia.food.PanFriedFoods;
import com.aura.ambrosia.item.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * While pan-frying, hides the offhand food in first person and renders a copy
 * of it sitting inside the frying pan instead.
 */
@Mixin(ItemInHandRenderer.class)
public abstract class FryingPanFirstPersonMixin {
    @Unique
    private static final ThreadLocal<Boolean> ambrosia$renderingPanFood = ThreadLocal.withInitial(() -> false);
    @Unique
    private static final ThreadLocal<Boolean> ambrosia$panPushed = ThreadLocal.withInitial(() -> false);

    @Inject(method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V",
            at = @At("HEAD"), cancellable = true)
    private void ambrosia$hideOffhandWhileFrying(LivingEntity entity, ItemStack stack,
                                                 ItemDisplayContext context, PoseStack poses,
                                                 SubmitNodeCollector collector, int light,
                                                 CallbackInfo ci) {
        if (ambrosia$renderingPanFood.get()) {
            return;
        }
        if (!ambrosia$isCooking(entity)) {
            return;
        }
        // Hide the offhand food itself — it will be drawn inside the pan instead.
        ItemStack offhand = entity.getOffhandItem();
        if (!offhand.isEmpty() && ItemStack.isSameItemSameComponents(stack, offhand)) {
            ci.cancel();
            return;
        }
        // Extra pose for the pan itself while right-clicking.
        if (stack.is(ModItems.FRYING_PAN)) {
            poses.pushPose();
            ambrosia$panPushed.set(true);
            try {
                poses.translate(0.85970944F, 0.0F, 0.0F);
                poses.mulPose(Axis.YP.rotationDegrees(-43.340916F));
                poses.mulPose(Axis.ZP.rotationDegrees(2.1358626F));
            } catch (Exception e) {
                poses.popPose();
                ambrosia$panPushed.set(false);
                throw e;
            }
        }
    }

    @Inject(method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V",
            at = @At("RETURN"))
    private void ambrosia$renderFoodInPan(LivingEntity entity, ItemStack stack,
                                          ItemDisplayContext context, PoseStack poses,
                                          SubmitNodeCollector collector, int light,
                                          CallbackInfo ci) {
        if (ambrosia$renderingPanFood.get()) {
            return;
        }
        try {
            if (!stack.is(ModItems.FRYING_PAN) || !ambrosia$isCooking(entity)) {
                return;
            }
            ItemStack food = entity.getOffhandItem();
            if (food.isEmpty() || !PanFriedFoods.isUncookedFoodClient(food)) {
                return;
            }
            poses.pushPose();
            try {
                poses.translate(-0.1229131F, 0.06811858F, 0.02F);
                poses.mulPose(Axis.XP.rotationDegrees(1.0161601F));
                poses.mulPose(Axis.YP.rotationDegrees(92.95698F));
                poses.mulPose(Axis.ZP.rotationDegrees(-24.619007F));
                poses.scale(1.0004175F, 0.9998395F, 1.0004816F);
                ambrosia$renderingPanFood.set(true);
                try {
                    ((ItemInHandRenderer) (Object) this).renderItem(entity, food, context, poses, collector, light);
                } finally {
                    ambrosia$renderingPanFood.set(false);
                }
            } finally {
                poses.popPose();
            }
        } finally {
            if (ambrosia$panPushed.get()) {
                ambrosia$panPushed.set(false);
                poses.popPose();
            }
        }
    }

    @Unique
    private static boolean ambrosia$isCooking(LivingEntity entity) {
        return entity != null && entity.isUsingItem() && entity.getUseItem().is(ModItems.FRYING_PAN);
    }
}
