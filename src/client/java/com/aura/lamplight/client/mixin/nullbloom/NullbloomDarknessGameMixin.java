package com.aura.lamplight.client.mixin.nullbloom;

import com.aura.lamplight.nullbloom.client.NullbloomDarkness;
import com.aura.lamplight.nullbloom.client.god.GodFxPass;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GameRenderer.class, priority = 900)
public class NullbloomDarknessGameMixin {
	@Inject(method = "renderLevel", at = @At(value = "INVOKE",
		target = "Lcom/mojang/blaze3d/systems/CommandEncoder;clearDepthTexture(Lcom/mojang/blaze3d/textures/GpuTexture;D)V"))
	private void nullbloom$darkness(DeltaTracker deltaTracker, CallbackInfo ci) {
		NullbloomDarkness.render(Minecraft.getInstance());
		GodFxPass.render(Minecraft.getInstance());
	}
}
