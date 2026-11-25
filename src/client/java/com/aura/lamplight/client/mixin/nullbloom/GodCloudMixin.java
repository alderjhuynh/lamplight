package com.aura.lamplight.client.mixin.nullbloom;

import com.aura.lamplight.nullbloom.client.god.GodSequence;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.renderer.CloudRenderer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CloudRenderer.class)
public class GodCloudMixin {
	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private void god$noVanillaClouds(int color, CloudStatus cloudStatus, float bottomY, int range, Vec3 cameraPosition,
		long gameTime, float partialTicks, CallbackInfo ci) {
		if (GodSequence.godSkyActive()) {
			ci.cancel();
		}
	}
}
