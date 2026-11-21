package com.aura.lamplight.client.mixin.impactframe;

import com.aura.lamplight.client.impactframe.ImpactFrame;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class ImpactFrameGameMixin {
    @Inject(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/fog/FogRenderer;endFrame()V",
            shift = At.Shift.AFTER))
    private void lamplight$impactFrame(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        ImpactFrame.render(Minecraft.getInstance());
    }
}
