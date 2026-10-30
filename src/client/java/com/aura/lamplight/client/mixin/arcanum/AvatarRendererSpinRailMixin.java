package com.aura.lamplight.client.mixin.arcanum;

import com.aura.lamplight.arcanum.client.SpinRailRenderState;
import com.aura.lamplight.arcanum.enchantment.SpinAttackRailData;
import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public class AvatarRendererSpinRailMixin {

    @Inject(
        method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
        at = @At("TAIL")
    )
    private void arcanum$extractSpinRail(Avatar entity, AvatarRenderState state, float tickDelta, CallbackInfo ci) {
        boolean inferno = entity instanceof SpinAttackRailData rail && rail.arcanum$isInfernoSpinRail();
        ((FabricRenderState) (Object) state).setData(SpinRailRenderState.INFERNO_SPIN_RAIL, inferno);
    }
}
