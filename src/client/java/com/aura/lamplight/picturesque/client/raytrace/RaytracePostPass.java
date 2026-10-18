package com.aura.lamplight.picturesque.client.raytrace;

import com.aura.lamplight.picturesque.Picturesque;
import com.aura.lamplight.picturesque.client.PicturesqueState;
import com.aura.lamplight.client.mixin.picturesque.raytrace.GameRendererAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.Identifier;

public final class RaytracePostPass {
    public static final Identifier CHAIN_ID = Identifier.fromNamespaceAndPath("lamplight", "raytrace");

    private static boolean warned;

    private RaytracePostPass() {
    }

    @SuppressWarnings("deprecation")
    public static void render(Minecraft client) {
        if (!RaytraceState.get().active() || client.level == null) {
            return;
        }
        if (PicturesqueState.get().config() == null || !PicturesqueState.get().config().raytraceBloom) {
            return;
        }
        if (client.gameRenderer == null) {
            return;
        }
        try {
            PostChain chain = client.getShaderManager().getPostChain(CHAIN_ID, LevelTargetBundle.MAIN_TARGETS);
            if (chain == null) {
                warnOnce("raytrace post chain missing, skipping bloom");
                return;
            }
            GameRendererAccessor access = (GameRendererAccessor) client.gameRenderer;
            chain.process(access.picturesque$getMainRenderTarget(), access.picturesque$getResourcePool());
            warned = false;
        } catch (RuntimeException e) {
            warnOnce("raytrace post pass failed, skipping until it works again: " + e.getMessage());
        }
    }

    private static void warnOnce(String message) {
        if (!warned) {
            warned = true;
            Picturesque.LOGGER.warn("[picturesque] {}", message);
        }
    }
}
