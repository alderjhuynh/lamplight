package com.aura.lamplight.client.mixin.picturesque.raytrace;

import com.aura.lamplight.picturesque.client.raytrace.CausticField;
import com.aura.lamplight.picturesque.client.raytrace.RaytraceState;
import com.aura.lamplight.picturesque.client.raytrace.VibrantLight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import net.minecraft.core.BlockPos;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightmapRenderStateExtractor.class)
public class RaytraceLightmapMixin {
    @Inject(method = "extract", at = @At("RETURN"))
    private void picturesque$raytraceLight(LightmapRenderState state, float tickDelta, CallbackInfo ci) {
        RaytraceState rt = RaytraceState.get();
        if (!rt.active()) {
            return;
        }
        float strength = rt.raytraceStrength();
        if (strength <= 0.001f) {
            return;
        }
        float vibrant = rt.vibrantBoost() * strength;

        if (vibrant > 0.001f) {
            state.blockLightTint = saturateVec(state.blockLightTint, vibrant * 0.55f);
            state.skyLightColor = saturateVec(state.skyLightColor, vibrant * 0.65f);
            state.ambientColor = saturateVec(state.ambientColor, vibrant * 0.45f);
        }

        if (strength > 0.001f) {
            state.ambientColor = contrastVec(state.ambientColor, strength * 0.5f);
        }

        try {
            Minecraft client = Minecraft.getInstance();
            if (client.gameRenderer.mainCamera() != null
                    && client.gameRenderer.mainCamera().getFluidInCamera()
                            == net.minecraft.world.level.material.FogType.WATER) {
                BlockPos cam = client.gameRenderer.mainCamera().blockPosition();
                float shimmer = CausticField.shimmer(cam.getX(), cam.getY(), cam.getZ(), rt.timeSeconds());
                float breathe = 1.0f + (shimmer - 0.55f) * 0.35f * strength;
                state.ambientColor = new Vector3f(
                        state.ambientColor.x() * breathe,
                        state.ambientColor.y() * breathe,
                        state.ambientColor.z() * breathe);
            }
        } catch (Exception e) {
        }
    }

    private static Vector3f saturateVec(org.joml.Vector3fc from, float amount) {
        float[] out = new float[3];
        VibrantLight.saturate(from.x(), from.y(), from.z(), amount, out);
        return new Vector3f(out[0], out[1], out[2]);
    }

    private static Vector3f contrastVec(org.joml.Vector3fc from, float amount) {
        float[] out = new float[3];
        VibrantLight.realisticContrast(from.x(), from.y(), from.z(), amount, out);
        return new Vector3f(out[0], out[1], out[2]);
    }
}
