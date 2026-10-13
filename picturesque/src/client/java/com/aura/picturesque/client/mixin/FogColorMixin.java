package com.aura.picturesque.client.mixin;

import com.aura.picturesque.client.PicturesqueState;
import com.aura.picturesque.client.color.FogTint;
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
public class FogColorMixin {
    @Inject(method = "setupFog", at = @At("RETURN"))
    private void picturesque$warmFog(Camera camera, int renderDistance, DeltaTracker deltaTracker,
            float tickDelta, ClientLevel level,
            CallbackInfoReturnable<FogData> cir) {
        PicturesqueState state = PicturesqueState.get();
        if (!state.active()) {
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
        float fogStrength = state.fogStrength();
        float gradeStrength = state.gradeStrength();
        if (fogStrength <= 0.001f && gradeStrength <= 0.001f) {
            return;
        }
        int vanilla = pack(data.color.x, data.color.y, data.color.z);
        int warmed = FogTint.blend(vanilla, state.mood(), state.biomeKind(), fogStrength, gradeStrength);
        data.color.set(red(warmed), green(warmed), blue(warmed), data.color.w);
    }

    private static int pack(float r, float g, float b) {
        return (clamp(r) << 16) | (clamp(g) << 8) | clamp(b);
    }

    private static int clamp(float v) {
        int i = Math.round(v * 255.0f);
        return i < 0 ? 0 : (i > 255 ? 255 : i);
    }

    private static float red(int packed) {
        return ((packed >> 16) & 0xFF) / 255.0f;
    }

    private static float green(int packed) {
        return ((packed >> 8) & 0xFF) / 255.0f;
    }

    private static float blue(int packed) {
        return (packed & 0xFF) / 255.0f;
    }
}
