package com.aura.lamplight.mixin.panacea;

import com.aura.lamplight.panacea.registry.ModEffects;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class SinkingMixin {

	@Inject(method = "shouldTravelInFluid", at = @At("HEAD"), cancellable = true)
	private void panacea$sinkThroughWater(FluidState fluidState, CallbackInfoReturnable<Boolean> cir) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (self.hasEffect(ModEffects.SINKING) && fluidState.is(FluidTags.WATER)) {
			cir.setReturnValue(false);
		}
	}
}
