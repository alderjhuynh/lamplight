package com.aura.picturesque.client.mixin;

import com.aura.picturesque.client.PicturesqueConfig;
import com.aura.picturesque.client.PicturesqueState;
import com.aura.picturesque.client.color.GouacheGrade;
import com.aura.picturesque.client.color.LightMood;
import com.aura.picturesque.client.color.PicturesquePalette;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Warm light without simulation: leans the lightmap toward buttery sun by
 * day, amber lantern pools by night, and periwinkle/mauve ambient shadows.
 *
 * <p>Block light (torches, lanterns, fireplaces) gets the generous amber
 * spill that makes interiors inviting; sky light follows the mood's sun
 * tint; ambient/bounce leans grass-green by day and lantern-warm at night.
 * Everything is a gentle lerp plus the shared gouache grade — no hard
 * contrast, no crushed blacks.
 */
@Mixin(LightmapRenderStateExtractor.class)
public class LightmapMixin {
    @Inject(method = "extract", at = @At("RETURN"))
    private void picturesque$warmLight(LightmapRenderState state, float tickDelta, CallbackInfo ci) {
        PicturesqueState pic = PicturesqueState.get();
        if (!pic.active()) {
            return;
        }
        PicturesqueConfig config = pic.config();
        if (config == null) {
            return;
        }
        float grade = pic.gradeStrength();
        LightMood mood = pic.mood();

        if (config.warmLight) {
            // Lantern amber takes over as night falls; sun tint leads by day.
            int blockTarget = PicturesquePalette.lerp(
                    mood.sunTint(), PicturesquePalette.LANTERN_AMBER, mood.lanternBoost() * 0.75f);
            state.blockLightTint = lerpVec(state.blockLightTint, blockTarget, 0.35f);
            state.skyLightColor = lerpVec(state.skyLightColor, mood.sunTint(), 0.30f);
        }
        if (config.bounceLight) {
            int ambientTarget = config.cozyShadows
                    ? PicturesquePalette.lerp(mood.ambientTint(), mood.shadowTint(), 0.25f)
                    : mood.ambientTint();
            state.ambientColor = lerpVec(state.ambientColor, ambientTarget, 0.30f);
        }
        if (grade > 0.001f) {
            state.blockLightTint = gradeVec(state.blockLightTint, grade * 0.6f);
            state.skyLightColor = gradeVec(state.skyLightColor, grade * 0.6f);
            state.ambientColor = gradeVec(state.ambientColor, grade * 0.6f);
        }
    }

    private static Vector3f lerpVec(org.joml.Vector3fc from, int toPacked, float t) {
        float r = from.x() + (PicturesquePalette.red(toPacked) - from.x()) * t;
        float g = from.y() + (PicturesquePalette.green(toPacked) - from.y()) * t;
        float b = from.z() + (PicturesquePalette.blue(toPacked) - from.z()) * t;
        return new Vector3f(r, g, b);
    }

    private static Vector3f gradeVec(org.joml.Vector3fc from, float strength) {
        float[] out = new float[3];
        GouacheGrade.grade(from.x(), from.y(), from.z(), strength, out);
        return new Vector3f(out[0], out[1], out[2]);
    }
}
