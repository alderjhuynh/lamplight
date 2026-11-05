package com.aura.lamplight.client.mixin.armory;

import com.aura.lamplight.armory.client.ScytheArmPose;
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

@Mixin(ItemInHandRenderer.class)
public abstract class ScytheInHandDeltaMixin {
	@Unique
	private static final ThreadLocal<Boolean> armory$scythePushed = ThreadLocal.withInitial(() -> false);

	@Inject(method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V",
			at = @At("HEAD"))
	private void armory$pushScytheDelta(LivingEntity entity, ItemStack stack,
			ItemDisplayContext context, PoseStack poses,
			SubmitNodeCollector collector, int light, CallbackInfo ci) {
		if (context != ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
				&& context != ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
			return;
		}
		if (!ScytheArmPose.isScythe(stack) || (ScytheArmPose.fpRotX == 0.0F
				&& ScytheArmPose.fpRotY == 0.0F && ScytheArmPose.fpRotZ == 0.0F
				&& ScytheArmPose.fpTransX == 0.0F && ScytheArmPose.fpTransY == 0.0F
				&& ScytheArmPose.fpTransZ == 0.0F && ScytheArmPose.fpScale == 1.0F)) {
			return;
		}
		poses.pushPose();
		armory$scythePushed.set(true);
		try {
			poses.translate(ScytheArmPose.fpTransX, ScytheArmPose.fpTransY, ScytheArmPose.fpTransZ);
			poses.mulPose(Axis.XP.rotationDegrees(ScytheArmPose.fpRotX));
			poses.mulPose(Axis.YP.rotationDegrees(ScytheArmPose.fpRotY));
			poses.mulPose(Axis.ZP.rotationDegrees(ScytheArmPose.fpRotZ));
			poses.scale(ScytheArmPose.fpScale, ScytheArmPose.fpScale, ScytheArmPose.fpScale);
		} catch (Exception e) {
			poses.popPose();
			armory$scythePushed.set(false);
			throw e;
		}
	}

	@Inject(method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V",
			at = @At("RETURN"))
	private void armory$popScytheDelta(LivingEntity entity, ItemStack stack,
			ItemDisplayContext context, PoseStack poses,
			SubmitNodeCollector collector, int light, CallbackInfo ci) {
		if (armory$scythePushed.get()) {
			armory$scythePushed.set(false);
			poses.popPose();
		}
	}
}
