package com.aura.lamplight.client.mixin.picturesque.raytrace;

import com.aura.lamplight.picturesque.client.raytrace.CausticField;
import com.aura.lamplight.picturesque.client.raytrace.RaytraceState;
import com.aura.lamplight.picturesque.client.raytrace.VibrantLight;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.environment.WaterFogEnvironment;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WaterFogEnvironment.class)
public class RaytraceWaterMixin {
    @Inject(method = "getBaseColor", at = @At("RETURN"), cancellable = true)
    private void picturesque$raytraceWater(ClientLevel level, Camera camera, int renderDistance,
            float tickDelta, CallbackInfoReturnable<Integer> cir) {
        RaytraceState rt = RaytraceState.get();
        if (!rt.active()) {
            return;
        }
        float strength = rt.raytraceStrength();
        if (strength <= 0.001f) {
            return;
        }
        int base = cir.getReturnValue();
        float r = VibrantLight.red(base);
        float g = VibrantLight.green(base);
        float b = VibrantLight.blue(base);

        try {
            Minecraft client = Minecraft.getInstance();
            if (client.gameRenderer.mainCamera() != null) {
                BlockPos cam = client.gameRenderer.mainCamera().blockPosition();
                float shimmer = CausticField.shimmer(cam.getX(), cam.getY(), cam.getZ(), rt.timeSeconds());
                float breathe = 1.0f + (shimmer - 0.55f) * 0.45f * strength * (0.35f + 0.65f * rt.daylight());
                r *= breathe;
                g *= breathe;
                b *= breathe;
            }
        } catch (Exception e) {
        }

        float[] out = new float[3];
        VibrantLight.saturate(r, g, b, rt.vibrantBoost() * strength * 0.4f, out);
        cir.setReturnValue(VibrantLight.pack(out[0], out[1], out[2]));
    }
}
