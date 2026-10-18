package com.aura.lamplight.client.mixin.picturesque;

import com.aura.lamplight.picturesque.client.PicturesqueState;
import com.aura.lamplight.picturesque.client.color.FogTint;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.environment.WaterFogEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WaterFogEnvironment.class)
public class WaterFogMixin {
    @Inject(method = "getBaseColor", at = @At("RETURN"), cancellable = true)
    private void picturesque$litWater(ClientLevel level, Camera camera, int renderDistance,
            float tickDelta, CallbackInfoReturnable<Integer> cir) {
        PicturesqueState pic = PicturesqueState.get();
        if (!pic.active() || pic.config() == null || !pic.config().jellyWater) {
            return;
        }
        cir.setReturnValue(FogTint.liftWater(cir.getReturnValue(), pic.gradeStrength()));
    }
}
