package com.aura.lamplight.client.mixin.picturesque.raytrace;

import com.aura.lamplight.picturesque.client.raytrace.RaytraceState;
import com.aura.lamplight.picturesque.client.raytrace.VibrantLight;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public class RaytraceFogMixin {
    @Inject(method = "setupFog", at = @At("RETURN"))
    private void picturesque$raytraceFog(Camera camera, int renderDistance, DeltaTracker deltaTracker,
            float tickDelta, ClientLevel level,
            CallbackInfoReturnable<FogData> cir) {
        RaytraceState rt = RaytraceState.get();
        if (!rt.active()) {
            return;
        }
        float strength = rt.raytraceStrength() * rt.vibrantBoost();
        if (strength <= 0.001f) {
            return;
        }
        net.minecraft.world.level.material.FogType fogType = camera.getFluidInCamera();
        if (fogType != net.minecraft.world.level.material.FogType.ATMOSPHERIC
                && fogType != net.minecraft.world.level.material.FogType.NONE) {
            return;
        }
        FogData data = cir.getReturnValue();
        if (data == null || data.color == null) {
            return;
        }
        float[] out = new float[3];
        VibrantLight.saturate(data.color.x, data.color.y, data.color.z, strength * 0.45f, out);
        data.color.set(out[0], out[1], out[2], data.color.w);
    }
}
