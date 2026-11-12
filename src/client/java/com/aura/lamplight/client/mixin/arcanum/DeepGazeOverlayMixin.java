package com.aura.lamplight.client.mixin.arcanum;

import com.aura.lamplight.arcanum.enchantment.ArcanumEnchantmentHelper;
import com.aura.lamplight.arcanum.enchantment.ArcanumEnchantments;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
public class DeepGazeOverlayMixin {

    @Inject(method = "submitWater", at = @At("HEAD"), cancellable = true)
    private static void lamplight$deepGazeClearWater(Minecraft minecraft, PoseStack poseStack,
                                                     SubmitNodeCollector collector, CallbackInfo ci) {
        LocalPlayer player = minecraft.player;
        if (player != null && ArcanumEnchantmentHelper.wearsEnchantment(player, ArcanumEnchantments.DEEP_GAZE)) {
            ci.cancel();
        }
    }
}
