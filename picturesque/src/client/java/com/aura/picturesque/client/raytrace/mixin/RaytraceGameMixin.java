package com.aura.picturesque.client.raytrace.mixin;

import com.aura.picturesque.client.raytrace.RaytracePostPass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class RaytraceGameMixin {
    @Inject(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/fog/FogRenderer;endFrame()V",
            shift = At.Shift.AFTER))
    private void picturesque$raytracePost(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        RaytracePostPass.render(Minecraft.getInstance());
    }
}
