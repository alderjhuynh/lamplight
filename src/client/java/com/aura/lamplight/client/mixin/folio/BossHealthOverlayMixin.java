package com.aura.lamplight.client.mixin.folio;

import com.aura.lamplight.folio.client.MulticastBarClientState;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.world.BossEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BossHealthOverlay.class)
public abstract class BossHealthOverlayMixin {

    private static final int BAR_WIDTH = 182;

    @Inject(
        method = "extractBar(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IILnet/minecraft/world/BossEvent;)V",
        at = @At("TAIL")
    )
    private void lamplight$drawMulticastSections(
        final GuiGraphicsExtractor graphics,
        final int x,
        final int y,
        final BossEvent event,
        final CallbackInfo ci
    ) {
        int sections = MulticastBarClientState.sections(event.getId());
        if (sections <= 1) {
            return;
        }
        for (int i = 1; i < sections; i++) {
            int lineX = x + Math.round(BAR_WIDTH * (float) i / sections);
            graphics.fill(lineX, y + 1, lineX + 1, y + 4, 0xCC000000);
        }
    }
}
