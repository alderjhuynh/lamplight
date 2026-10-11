package com.aura.picturesque.client.mixin;

import com.aura.picturesque.client.PicturesqueState;
import com.aura.picturesque.client.color.FogTint;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.environment.WaterFogEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lit-up water: brightens the underwater fog base the way darkness is
 * treated — more light, same hue — instead of repainting it.
 *
 * <p>Vantage's deep teals stay teal, just less murky, with a faint aqua
 * breath so dives feel dreamy. Depth cueing still works, and the
 * atmospheric fog pass ({@code FogColorMixin}) leaves water alone, so this
 * is the only hand on the color. Blurred sky reflections, caustics, and
 * stained-glass colored light remain future passes.
 */
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
