package com.aura.lamplight.client.mixin.armory;

import com.aura.lamplight.armory.client.ScytheArmPose;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelScythePoseMixin<T extends HumanoidRenderState> {

	@Shadow
	public ModelPart rightArm;
	@Shadow
	public ModelPart leftArm;

	@Inject(method = "setupAnim", at = @At("TAIL"))
	private void armory$applyScythePose(final T state, final CallbackInfo ci) {
		if (state.isUsingItem || state.attackTime != 0.0F || state.isVisuallySwimming) {
			return;
		}
		if (ScytheArmPose.isScythe(state.rightHandItemStack)) {
			ScytheArmPose.applyPose(this.rightArm, false);
		}
		if (ScytheArmPose.isScythe(state.leftHandItemStack)) {
			ScytheArmPose.applyPose(this.leftArm, true);
		}
	}
}
