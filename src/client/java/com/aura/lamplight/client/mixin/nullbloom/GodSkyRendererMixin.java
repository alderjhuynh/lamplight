package com.aura.lamplight.client.mixin.nullbloom;

import com.aura.lamplight.nullbloom.client.god.GodSequence;
import com.aura.lamplight.nullbloom.client.god.GodSkyRenderer;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.world.level.MoonPhase;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkyRenderer.class)
public class GodSkyRendererMixin {
	@Shadow
	@Final
	private RenderTarget renderTarget;

	@Inject(method = "renderSkyDisc", at = @At("HEAD"), cancellable = true)
	private void god$replaceSky(int skyColor, CallbackInfo ci) {
		if (GodSequence.godSkyActive() && GodSkyRenderer.render(this.renderTarget, GodSequence.flashYaw())) {
			ci.cancel();
		}
	}

	@Inject(method = "renderSunriseAndSunset", at = @At("HEAD"), cancellable = true)
	private void god$noSunriseGlow(PoseStack poseStack, float sunAngle, int color, CallbackInfo ci) {
		if (GodSequence.godSkyActive()) {
			ci.cancel();
		}
	}

	@Inject(method = "renderSunMoonAndStars", at = @At("HEAD"), cancellable = true)
	private void god$noCelestialBodies(PoseStack poseStack, float sunAngle, float moonAngle, float starAngle,
		MoonPhase moonPhase, float rainBrightness, float starBrightness, CallbackInfo ci) {
		if (GodSequence.godSkyActive()) {
			ci.cancel();
		}
	}

	@Inject(method = "renderDarkDisc", at = @At("HEAD"), cancellable = true)
	private void god$noDarkDisc(CallbackInfo ci) {
		if (GodSequence.godSkyActive()) {
			ci.cancel();
		}
	}

	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void god$timeLapse(ClientLevel level, float partialTicks, Camera camera, SkyRenderState state, CallbackInfo ci) {
		GodSequence.applySkyOverrides(level, state);
	}
}
