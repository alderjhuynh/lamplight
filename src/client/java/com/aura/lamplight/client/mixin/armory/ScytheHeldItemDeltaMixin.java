package com.aura.lamplight.client.mixin.armory;

import com.aura.lamplight.armory.client.ScytheArmPose;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public abstract class ScytheHeldItemDeltaMixin {
	@Unique
	private boolean armory$scythePushed;

	@Inject(method = "submitArmWithItem",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V"))
	private void armory$pushHeldDelta(ArmedEntityRenderState state, ItemStackRenderState itemState,
			ItemStack stack, HumanoidArm arm, PoseStack poses,
			SubmitNodeCollector collector, int light, CallbackInfo ci) {
		boolean left = arm == HumanoidArm.LEFT;
		if (ScytheArmPose.isScythe(stack) && !ScytheArmPose.tpDeltaNeutral(left)) {
			poses.pushPose();
			armory$scythePushed = true;
			float rotX = left ? ScytheArmPose.tpLeftRotX : ScytheArmPose.tpRotX;
			float rotY = left ? ScytheArmPose.tpLeftRotY : ScytheArmPose.tpRotY;
			float rotZ = left ? ScytheArmPose.tpLeftRotZ : ScytheArmPose.tpRotZ;
			float transX = left ? ScytheArmPose.tpLeftTransX : ScytheArmPose.tpTransX;
			float transY = left ? ScytheArmPose.tpLeftTransY : ScytheArmPose.tpTransY;
			float transZ = left ? ScytheArmPose.tpLeftTransZ : ScytheArmPose.tpTransZ;
			float scale = left ? ScytheArmPose.tpLeftScale : ScytheArmPose.tpScale;
			poses.translate(transX, transY, transZ);
			poses.mulPose(Axis.XP.rotationDegrees(rotX));
			poses.mulPose(Axis.YP.rotationDegrees(rotY));
			poses.mulPose(Axis.ZP.rotationDegrees(rotZ));
			poses.scale(scale, scale, scale);
		}
	}

	@Inject(method = "submitArmWithItem",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V",
					shift = At.Shift.AFTER))
	private void armory$popHeldDelta(ArmedEntityRenderState state, ItemStackRenderState itemState,
			ItemStack stack, HumanoidArm arm, PoseStack poses,
			SubmitNodeCollector collector, int light, CallbackInfo ci) {
		if (armory$scythePushed) {
			armory$scythePushed = false;
			poses.popPose();
		}
	}
}
