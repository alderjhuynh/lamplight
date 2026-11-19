package com.aura.lamplight.client.mixin.folio;

import com.aura.lamplight.folio.client.DragonbladeVignette;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class DragonbladeAimMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void lamplight$grayOutline(Entity entity, EntityRenderState state, float partialTick, CallbackInfo ci) {
        if (!DragonbladeVignette.isActive()) return;
        if (!DragonbladeVignette.isTeleported(entity.getId())) return;
        // only when currently aiming at this entity
        Minecraft mc = Minecraft.getInstance();
        HitResult hr = mc.hitResult;
        if (hr instanceof EntityHitResult ehr && ehr.getEntity() == entity) {
            state.outlineColor = 0xFF808080; // gray outline
        }
    }
}
