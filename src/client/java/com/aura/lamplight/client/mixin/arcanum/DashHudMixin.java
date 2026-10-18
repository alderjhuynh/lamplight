package com.aura.lamplight.client.mixin.arcanum;

import com.aura.lamplight.arcanum.client.ability.Dash;
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
public class DashHudMixin {

    private static final int FRAME_COUNT = 10;
    private static final int TEX_SIZE = 9;

    private static final Identifier[] DASH_TEXTURES = new Identifier[FRAME_COUNT];
    static {
        for (int i = 0; i < FRAME_COUNT; i++) {
            DASH_TEXTURES[i] = Identifier.fromNamespaceAndPath(
                    "lamplight",
                    "dash/icon/dash" + (i + 1) + ".png"
            );
        }
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void arcanum$renderDashCooldown(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;
        if (!ArcanumEnchantmentHelper.hasDash(client.player)) return;

        int cooldown = Dash.getCooldownTimer();
        int maxCooldown = Dash.getCooldownTicks();

        if (cooldown == 0) return;

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();

        int x = (screenWidth - TEX_SIZE) / 2;
        int y = (screenHeight / 2) + 15;

        int frameIndex = computeFrameIndex(cooldown, maxCooldown);

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                DASH_TEXTURES[frameIndex],
                x, y,
                0.0F, 0.0F,
                TEX_SIZE, TEX_SIZE,
                TEX_SIZE, TEX_SIZE
        );
    }

    private int computeFrameIndex(int cooldown, int maxCooldown) {
        if (cooldown <= 0) {
            return FRAME_COUNT - 1;
        }
        double progress = (double) cooldown / maxCooldown;
        int index = (int) Math.round(progress * (FRAME_COUNT - 1));
        return (FRAME_COUNT - 1) - clamp(index, 0, FRAME_COUNT - 1);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
