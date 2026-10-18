package com.aura.lamplight.client.mixin.menagerie;

import com.aura.lamplight.menagerie.client.CrowShoulderRenderState;
import com.aura.lamplight.menagerie.crow.CrowShoulderData;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void menagerie$extractCrowShoulders(Avatar avatar, AvatarRenderState state, float partialTicks, CallbackInfo ci) {
        if (avatar instanceof CrowShoulderData data && state instanceof CrowShoulderRenderState crowState) {
            crowState.menagerie$setCrowOnLeftShoulder(data.menagerie$hasCrowOnLeftShoulder());
            crowState.menagerie$setCrowOnRightShoulder(data.menagerie$hasCrowOnRightShoulder());
        }
    }
}
