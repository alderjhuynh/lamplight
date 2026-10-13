package com.aura.picturesque.client.mixin;

import com.aura.picturesque.client.PicturesqueState;
import com.aura.picturesque.client.color.GouacheGrade;
import com.aura.picturesque.client.color.PicturesquePalette;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkyRenderer.class)
public class SkyColorMixin {
    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void picturesque$warmSky(ClientLevel level, float tickDelta, Camera camera,
            SkyRenderState state, CallbackInfo ci) {
        PicturesqueState pic = PicturesqueState.get();
        if (!pic.active()) {
            return;
        }
        float grade = pic.gradeStrength();
        boolean skyOn = pic.config() != null && pic.config().marshmallowSky;
        if (!skyOn && grade <= 0.001f) {
            return;
        }
        float skyMix = skyOn ? 0.10f + 0.15f * pic.fogStrength() : 0.0f;
        if (skyMix > 0.001f) {
            state.skyColor = PicturesquePalette.lerp(state.skyColor, pic.mood().skyTint(), skyMix);
            int duskBand = PicturesquePalette.lerp(
                    PicturesquePalette.EDGE_CORAL, PicturesquePalette.EDGE_ROSY_LAVENDER, 0.5f);
            state.sunriseAndSunsetColor = PicturesquePalette.lerp(
                    state.sunriseAndSunsetColor, duskBand, skyMix);
        }
        if (grade > 0.001f) {
            state.skyColor = GouacheGrade.gradePacked(state.skyColor, grade * 0.7f);
            state.sunriseAndSunsetColor = GouacheGrade.gradePacked(state.sunriseAndSunsetColor, grade * 0.7f);
        }
    }
}
