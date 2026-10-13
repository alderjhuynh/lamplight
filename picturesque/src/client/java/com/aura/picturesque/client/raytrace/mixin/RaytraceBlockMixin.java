package com.aura.picturesque.client.raytrace.mixin;

import com.aura.picturesque.client.PicturesqueState;
import com.aura.picturesque.client.color.LightMood;
import com.aura.picturesque.client.color.PicturesquePalette;
import com.aura.picturesque.client.raytrace.BounceSampler;
import com.aura.picturesque.client.raytrace.CausticField;
import com.aura.picturesque.client.raytrace.RaytraceState;
import com.aura.picturesque.client.raytrace.SunPath;
import com.aura.picturesque.client.raytrace.VibrantLight;
import com.aura.picturesque.client.raytrace.VoxelTracer;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelLighter;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import com.mojang.blaze3d.vertex.QuadInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockModelLighter.class)
public class RaytraceBlockMixin {
    @Inject(method = "prepareQuadAmbientOcclusion", at = @At("RETURN"))
    private void picturesque$raytraceAo(BlockAndTintGetter level, BlockState state, BlockPos pos,
            BakedQuad quad, QuadInstance quadInstance, CallbackInfo ci) {
        picturesque$shade(level, state, pos, quad, quadInstance);
    }

    @Inject(method = "prepareQuadFlat", at = @At("RETURN"))
    private void picturesque$raytraceFlat(BlockAndTintGetter level, BlockState state, BlockPos pos, int tint,
            BakedQuad quad, QuadInstance quadInstance, CallbackInfo ci) {
        picturesque$shade(level, state, pos, quad, quadInstance);
    }

    private static void picturesque$shade(BlockAndTintGetter level, BlockState state, BlockPos pos,
            BakedQuad quad, QuadInstance quadInstance) {
        RaytraceState rt = RaytraceState.get();
        if (!rt.active()) {
            return;
        }
        float strength = rt.raytraceStrength();
        if (strength <= 0.001f) {
            return;
        }
        if (PicturesqueState.get().config() != null && !PicturesqueState.get().config().raytraceBake) {
            return;
        }
        float vibrant = rt.vibrantBoost();

        Direction face = quad.direction();
        float nx = face == null ? 0.0f : face.getStepX();
        float ny = face == null ? 1.0f : face.getStepY();
        float nz = face == null ? 0.0f : face.getStepZ();

        float day = rt.daylight();
        boolean skyWorld = rt.skyWorld();
        float[] sun = rt.sunDir();
        float sunI = !skyWorld ? 0.0f : day;
        float moonI = !skyWorld ? 0.0f : (1.0f - day) * 0.40f;
        boolean leaves = state.getBlock() instanceof LeavesBlock;
        float wrap = leaves ? 0.6f : 0.18f;
        float sunDiff = SunPath.wrapDiffuse(nx * sun[0] + ny * sun[1] + nz * sun[2], wrap);
        float moonDiff = SunPath.wrapDiffuse(-(nx * sun[0] + ny * sun[1] + nz * sun[2]), wrap);

        LightMood mood = PicturesqueState.get().mood();
        int noonSun = VibrantLight.saturatePacked(mood.sunTint(), vibrant);
        int warmSun = VibrantLight.sinkingSun(noonSun, PicturesquePalette.EDGE_CORAL, 1.0f - day * 1.4f);
        int moonCol = VibrantLight.saturatePacked(PicturesquePalette.MOON_SILVER, 0.35f * vibrant + 0.15f);

        boolean domIsSun = sunI >= moonI;
        float lx = domIsSun ? sun[0] : -sun[0];
        float ly = domIsSun ? sun[1] : -sun[1];
        float lz = domIsSun ? sun[2] : -sun[2];
        float intensity = domIsSun ? sunI : moonI;
        int domColor = !skyWorld ? mood.sunTint() : (domIsSun ? warmSun : moonCol);
        float secDiff = domIsSun ? moonDiff : sunDiff;
        float secI = (domIsSun ? moonI : sunI) * 0.5f;
        int secColor = domIsSun ? moonCol : warmSun;

        float sunR = VibrantLight.red(domColor);
        float sunG = VibrantLight.green(domColor);
        float sunB = VibrantLight.blue(domColor);
        float sunLuma = 0.2126f * sunR + 0.7152f * sunG + 0.0722f * sunB + 1e-4f;
        float secR = VibrantLight.red(secColor);
        float secG = VibrantLight.green(secColor);
        float secB = VibrantLight.blue(secColor);
        float secLuma = 0.2126f * secR + 0.7152f * secG + 0.0722f * secB + 1e-4f;

        float diff = SunPath.wrapDiffuse(nx * lx + ny * ly + nz * lz, wrap);

        float sunVis = 1.0f;
        float skyVis = 1.0f;
        try {
            if (diff > 0.02f && intensity > 0.02f) {
                float[] l = new float[]{lx, ly, lz};
                sunVis = VoxelTracer.traceSunVisibility(level, pos, l, VoxelTracer.SUN_TAPS);
            } else {
                sunVis = 0.0f;
            }
            skyVis = VoxelTracer.traceSkyVisibility(level, pos);
        } catch (Exception e) {
            sunVis = 1.0f;
            skyVis = 1.0f;
        }

        float sunAmt = diff * sunVis * intensity * strength;
        float secAmt = secDiff * secI * strength;

        float[] bounce = new float[]{1.0f, 1.0f, 1.0f};
        float bounceAmt = 0.0f;
        try {
            bounceAmt = BounceSampler.sampleBounce(level, pos, bounce);
        } catch (Exception e) {
            bounceAmt = 0.0f;
        }
        float bounceLuma = 0.2126f * bounce[0] + 0.7152f * bounce[1] + 0.0722f * bounce[2] + 1e-4f;

        int glass = -1;
        try {
            glass = BounceSampler.glassAbove(level, pos);
        } catch (Exception e) {
            glass = -1;
        }

        boolean water = false;
        boolean waterAbove = false;
        try {
            water = BounceSampler.isWater(level, pos);
            waterAbove = !water && ny > 0.5f && BounceSampler.waterAbove(level, pos);
        } catch (Exception e) {
            water = false;
            waterAbove = false;
        }

        int shadowTint = mood.shadowTint();
        float shR = VibrantLight.red(shadowTint);
        float shG = VibrantLight.green(shadowTint);
        float shB = VibrantLight.blue(shadowTint);
        float shLuma = 0.2126f * shR + 0.7152f * shG + 0.0722f * shB + 1e-4f;
        float coverage = diff * sunVis;
        if (secDiff > coverage) {
            coverage = secDiff;
        }
        float shadowAmt = (1.0f - coverage) * (skyWorld ? (0.30f + 0.25f * day) : 0.30f) * strength;

        float skyGain = 0.80f + 0.20f * skyVis;

        float glassR = 1.0f;
        float glassG = 1.0f;
        float glassB = 1.0f;
        float glassAmt = 0.0f;
        if (glass >= 0 && diff > 0.02f && intensity > 0.02f) {
            glassR = VibrantLight.red(glass);
            glassG = VibrantLight.green(glass);
            glassB = VibrantLight.blue(glass);
            float glassLuma = 0.2126f * glassR + 0.7152f * glassG + 0.0722f * glassB + 1e-4f;
            glassR /= glassLuma;
            glassG /= glassLuma;
            glassB /= glassLuma;
            glassAmt = 0.55f * strength * diff * sunVis * intensity;
        }

        int skyTint = VibrantLight.saturatePacked(mood.skyTint(), vibrant);
        float skyR = VibrantLight.red(skyTint);
        float skyG = VibrantLight.green(skyTint);
        float skyB = VibrantLight.blue(skyTint);

        for (int i = 0; i < 4; i++) {
            int packed;
            try {
                packed = quadInstance.getColor(i);
            } catch (Exception e) {
                return;
            }
            int alpha = (packed >>> 24) & 0xFF;
            float r = ((packed >> 16) & 0xFF) / 255.0f;
            float g = ((packed >> 8) & 0xFF) / 255.0f;
            float b = (packed & 0xFF) / 255.0f;

            r *= 1.0f + (sunR / sunLuma - 1.0f) * sunAmt;
            g *= 1.0f + (sunG / sunLuma - 1.0f) * sunAmt;
            b *= 1.0f + (sunB / sunLuma - 1.0f) * sunAmt;
            r *= 1.0f + (secR / secLuma - 1.0f) * secAmt;
            g *= 1.0f + (secG / secLuma - 1.0f) * secAmt;
            b *= 1.0f + (secB / secLuma - 1.0f) * secAmt;

            r *= 1.0f + (shR / shLuma - 1.0f) * shadowAmt;
            g *= 1.0f + (shG / shLuma - 1.0f) * shadowAmt;
            b *= 1.0f + (shB / shLuma - 1.0f) * shadowAmt;

            r *= skyGain;
            g *= skyGain;
            b *= skyGain;

            if (bounceAmt > 0.0f) {
                float ba = 0.38f * strength;
                r *= 1.0f + (bounce[0] / bounceLuma - 1.0f) * ba;
                g *= 1.0f + (bounce[1] / bounceLuma - 1.0f) * ba;
                b *= 1.0f + (bounce[2] / bounceLuma - 1.0f) * ba;
            }

            if (glassAmt > 0.0f) {
                r *= 1.0f + (glassR - 1.0f) * glassAmt;
                g *= 1.0f + (glassG - 1.0f) * glassAmt;
                b *= 1.0f + (glassB - 1.0f) * glassAmt;
            }

            if (water) {
                float upness = ny > 0.0f ? ny : 0.0f;
                float skyMix = 0.30f * strength * upness;
                r += (skyR - r) * skyMix;
                g += (skyG - g) * skyMix;
                b += (skyB - b) * skyMix;
                float hx = lx;
                float hy = ly + 1.0f;
                float hz = lz;
                float hlen = (float) Math.sqrt(hx * hx + hy * hy + hz * hz) + 1e-4f;
                float spec = nx * hx / hlen + ny * hy / hlen + nz * hz / hlen;
                if (spec > 0.0f && intensity > 0.02f) {
                    float glint = (float) Math.pow(spec, 14.0) * sunVis * intensity * 0.65f * strength;
                    r += (1.0f - r) * glint;
                    g += (1.0f - g) * glint;
                    b += (1.0f - b) * glint;
                }
            } else if (waterAbove) {
                float wx = pos.getX() + quad.position(i).x();
                float wy = pos.getY() + quad.position(i).y();
                float wz = pos.getZ() + quad.position(i).z();
                float caustic = CausticField.staticPattern(wx, wy, wz);
                float ca = (caustic - 0.55f) * 0.55f * strength * (0.35f + 0.65f * day);
                r *= 1.0f + ca;
                g *= 1.0f + ca;
                b *= 1.0f + ca * 0.9f;
            }

            int ri = Math.round(clamp01(r) * 255.0f);
            int gi = Math.round(clamp01(g) * 255.0f);
            int bi = Math.round(clamp01(b) * 255.0f);
            try {
                quadInstance.setColor(i, (alpha << 24) | (ri << 16) | (gi << 8) | bi);
            } catch (Exception e) {
                return;
            }
        }
    }

    private static float clamp01(float v) {
        return v < 0.0f ? 0.0f : (v > 1.0f ? 1.0f : v);
    }
}
