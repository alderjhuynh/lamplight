package com.aura.lamplight.client.mixin.arcanum;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.SpinAttackEffectLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SpinAttackEffectLayer.class)
public class SpinAttackInfernoTextureMixin {

    private static final Identifier INFERNO_TEXTURE =
            Identifier.fromNamespaceAndPath("lamplight", "textures/entity/trident/trident_inferno.png");

    @WrapOperation(
        method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/resources/Identifier;IIILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"
        )
    )
    private void arcanum$infernoTexture(SubmitNodeCollector collector, Model<?> model, Object modelState, PoseStack poseStack, Identifier texture,
                                        int light, int overlay, int outlineColor,
                                        ModelFeatureRenderer.CrumblingOverlay crumbling,
                                        Operation<Void> op,
                                        @Local(ordinal = 0) AvatarRenderState state) {
        Identifier chosen = texture;
        if (state.displayFireAnimation) {
            chosen = INFERNO_TEXTURE;
        }
        op.call(collector, model, modelState, poseStack, chosen, light, overlay, outlineColor, crumbling);
    }
}
