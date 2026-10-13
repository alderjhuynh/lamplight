package com.aura.picturesque.client.raytrace.mixin;

import com.aura.picturesque.client.raytrace.RaytraceState;
import com.aura.picturesque.client.raytrace.VoxelTracer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public class RaytraceEntityMixin {
    private static final double TRACE_RANGE_SQR = 48.0 * 48.0;

    @Inject(method = "getPackedLightCoords", at = @At("RETURN"), cancellable = true)
    private <E extends Entity> void picturesque$shadowEntity(E entity, float partialTick,
            CallbackInfoReturnable<Integer> cir) {
        RaytraceState rt = RaytraceState.get();
        if (!rt.active() || !rt.skyWorld()) {
            return;
        }
        float strength = rt.raytraceStrength();
        if (strength <= 0.001f) {
            return;
        }
        int packed;
        try {
            packed = cir.getReturnValue();
        } catch (Exception e) {
            return;
        }
        int sky = LightCoordsUtil.sky(packed);
        if (sky <= 1) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.gameRenderer.mainCamera() == null) {
            return;
        }
        BlockPos camPos;
        BlockPos entPos;
        Level level;
        try {
            camPos = client.gameRenderer.mainCamera().blockPosition();
            entPos = entity.blockPosition();
            level = entity.level();
        } catch (Exception e) {
            return;
        }
        if (level != client.level) {
            return;
        }
        double dx = entPos.getX() - camPos.getX();
        double dy = entPos.getY() - camPos.getY();
        double dz = entPos.getZ() - camPos.getZ();
        if (dx * dx + dy * dy + dz * dz > TRACE_RANGE_SQR) {
            return;
        }

        float day = rt.daylight();
        float sunI = day;
        float moonI = (1.0f - day) * 0.5f;
        boolean useMoon = moonI > sunI;
        float[] sun = rt.sunDir();
        float[] dir = useMoon
                ? new float[]{-sun[0], -sun[1], -sun[2]}
                : new float[]{sun[0], sun[1], sun[2]};
        float energy = useMoon ? moonI : sunI;
        if (energy <= 0.03f) {
            return;
        }

        float vis;
        try {
            if (level.canSeeSky(entPos)) {
                return;
            }
            BlockPos eye = entPos.above(1);
            vis = VoxelTracer.traceSunVisibility(level, eye, dir, 2);
        } catch (Exception e) {
            return;
        }
        float keep = (0.30f + 0.70f * vis) * energy + (1.0f - energy);
        keep = 1.0f - (1.0f - keep) * strength;
        int shadowed = Math.round(sky * keep);
        if (shadowed < 0) {
            shadowed = 0;
        } else if (shadowed > 15) {
            shadowed = 15;
        }
        if (shadowed != sky) {
            cir.setReturnValue((packed & ~(15 << 20)) | (shadowed << 20));
        }
    }
}
