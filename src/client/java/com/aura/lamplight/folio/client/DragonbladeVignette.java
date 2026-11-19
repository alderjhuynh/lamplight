package com.aura.lamplight.folio.client;

import com.aura.lamplight.folio.Folio;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.Minecraft;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

public final class DragonbladeVignette implements HudElement {
    private static final Identifier VIGNETTE = Folio.id("textures/gui/teleport_vignette.png");
    private static volatile boolean active = false;
    private static final Set<Integer> teleported = new HashSet<>();
    private static volatile int aimingId = -1;

    public static void setActive(boolean value) {
        active = value;
        if (!value) clearTeleported();
    }

    public static boolean isActive() {
        return active;
    }

    public static void addTeleported(int id) { teleported.add(id); }
    public static void clearTeleported() { teleported.clear(); aimingId = -1; }
    public static boolean isTeleported(int id) { return teleported.contains(id); }
    public static void setAimingId(int id) { aimingId = id; }
    public static int getAimingId() { return aimingId; }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, DeltaTracker deltaTracker) {
        if (!active) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        float alpha = 0.35F;
        g.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, VIGNETTE,
                0, 0, 0.0F, 0.0F,
                g.guiWidth(), g.guiHeight(), g.guiWidth(), g.guiHeight(),
                ARGB.colorFromFloat(alpha, 0.55F, 0.15F, 0.85F));
    }
}
