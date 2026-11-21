package com.aura.lamplight.client.mixin.folio;

import com.aura.lamplight.folio.client.DragonbladeGrayscaleAccessor;
import com.aura.lamplight.folio.client.DragonbladeGrayscaleRenderTypes;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
@SuppressWarnings ("all")
public abstract class DragonbladeRenderTypeMixin {
    @Inject(method = "getRenderType", at = @At("RETURN"), cancellable = true)
    private void lamplight$swapGrayscale(LivingEntityRenderState state, boolean bodyVisible, boolean translucent, boolean glowing, CallbackInfoReturnable<RenderType> cir) {
        if (!((DragonbladeGrayscaleAccessor) state).lamplight$isDragonbladeGrayscale()) return;
        Identifier tex = (Identifier) ((LivingEntityRenderer) (Object) this).getTextureLocation(state);
        cir.setReturnValue(DragonbladeGrayscaleRenderTypes.get(tex));
    }
}
