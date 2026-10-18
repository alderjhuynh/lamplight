package com.aura.lamplight.client.mixin.arcanum;

import com.aura.lamplight.arcanum.client.ability.FlowGlide;
import com.aura.lamplight.arcanum.enchantment.ArcanumEnchantmentHelper;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public class FlowGlideHudMixin {

    private static final int TEX_SIZE = 15;

    private static final Identifier GLIDE_TEXTURE = Identifier.fromNamespaceAndPath(
            "lamplight",
            "textures/gui/sprites/crosshair/crosshair-flow-glide.png"
    );

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void arcanum$renderFlowGlideCrosshair(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!FlowGlide.isGliding) return;
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;
        if (!ArcanumEnchantmentHelper.hasFlowGlidePrereqs(client.player)) return;

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();

        int x = (screenWidth - TEX_SIZE) / 2;
        int y = Math.round((screenHeight / 2.0f) - 8.0f);

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                GLIDE_TEXTURE,
                x, y,
                0.0F, 0.0F,
                TEX_SIZE, TEX_SIZE,
                TEX_SIZE, TEX_SIZE
        );
    }
}
