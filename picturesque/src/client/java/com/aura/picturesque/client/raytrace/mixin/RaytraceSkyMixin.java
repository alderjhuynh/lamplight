package com.aura.picturesque.client.raytrace.mixin;

import com.aura.picturesque.client.color.PicturesquePalette;
import com.aura.picturesque.client.raytrace.RaytraceState;
import com.aura.picturesque.client.raytrace.VibrantLight;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkyRenderer.class)
public class RaytraceSkyMixin {
    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void picturesque$raytraceSky(ClientLevel level, float tickDelta, Camera camera,
            SkyRenderState state, CallbackInfo ci) {
        RaytraceState rt = RaytraceState.get();
        if (!rt.active()) {
            return;
        }
        float strength = rt.raytraceStrength();
        if (strength <= 0.001f) {
            return;
        }
        float vibrant = rt.vibrantBoost() * strength;
        float day = rt.daylight();

        state.skyColor = VibrantLight.saturatePacked(state.skyColor, vibrant * 0.7f);
        state.sunriseAndSunsetColor =
                VibrantLight.saturatePacked(state.sunriseAndSunsetColor, vibrant * 0.8f);

        float edge = day * (1.0f - day) * 4.0f;
        if (edge > 0.01f && rt.skyWorld()) {
            int rim = PicturesquePalette.lerp(
                    PicturesquePalette.SKY_GOLDEN_RIM, PicturesquePalette.EDGE_CORAL, 1.0f - day);
            state.sunriseAndSunsetColor =
                    PicturesquePalette.lerp(state.sunriseAndSunsetColor, rim, edge * 0.18f * strength);
        }

        if (day < 0.15f && rt.skyWorld()) {
            float depth = (0.15f - day) / 0.15f;
            state.skyColor = PicturesquePalette.lerp(
                    state.skyColor, PicturesquePalette.NIGHT_INDIGO, depth * 0.22f * strength);
        }
    }
}
