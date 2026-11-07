package com.aura.lamplight.client.mixin.puppeteer;

import com.aura.lamplight.puppeteer.client.AnimationClientState;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.joml.Quaternionfc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererAnimationMixin {

    @WrapOperation(
        method = "setupRotations",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionfc;)V",
            ordinal = 2
        )
    )
    private void puppeteer$neutralizeVanillaSwimFlip(
        final PoseStack poseStack,
        final Quaternionfc quaternion,
        final Operation<Void> original,
        final AvatarRenderState state,
        final PoseStack poseStack2,
        final float bodyRot,
        final float entityScale
    ) {
        AnimationClientState.Captured captured =
            ((FabricRenderState) state).getData(AnimationClientState.ANIMATION_RENDER_DATA);
        AnimationClientState.ActiveSet activeSet = captured == null ? null : captured.set();
        if (activeSet != null && !activeSet.layers().isEmpty()) {
            return;
        }
        original.call(poseStack, quaternion);
    }
}
