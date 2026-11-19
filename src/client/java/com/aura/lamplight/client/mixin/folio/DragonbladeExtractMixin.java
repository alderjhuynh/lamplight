package com.aura.lamplight.client.mixin.folio;

import com.aura.lamplight.folio.client.DragonbladeVignette;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import com.aura.lamplight.folio.client.DragonbladeGrayscaleAccessor;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class DragonbladeExtractMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void lamplight$markGrayscale(LivingEntity entity, LivingEntityRenderState state, float partialTick, CallbackInfo ci) {
        ((DragonbladeGrayscaleAccessor) state).lamplight$setDragonbladeGrayscale(false);
        if (!DragonbladeVignette.isActive()) return;
        if (!DragonbladeVignette.isTeleported(entity.getId())) return;
        Minecraft mc = Minecraft.getInstance();
        HitResult hr = mc.hitResult;
        if (hr instanceof EntityHitResult ehr && ehr.getEntity() == entity) {
            ((DragonbladeGrayscaleAccessor) state).lamplight$setDragonbladeGrayscale(true);
        }
    }
}
