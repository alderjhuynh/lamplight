package com.aura.lamplight.client.mixin.puppeteer;

import com.aura.lamplight.puppeteer.client.AnimationClientState;
import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class EntityRenderStateAnimationMixin<T extends LivingEntity, S extends LivingEntityRenderState> {

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void puppeteer$captureAnimationState(final T entity, final S state, final float partialTicks, final CallbackInfo ci) {
        AnimationClientState.Active active = AnimationClientState.get(entity.getId());
        ((FabricRenderState) state).setData(AnimationClientState.ANIMATION_RENDER_DATA, active);
    }
}
