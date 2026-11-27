package com.aura.lamplight.client.mixin.folio;

import com.aura.lamplight.folio.effect.Frozen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class FrozenMouseHandlerMixin {

    @Inject(method = "turnPlayer", at = @At("HEAD"), cancellable = true)
    private void folio$frozenNoLooking(final double timeDelta, final CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && Frozen.is(minecraft.player)) {
            ci.cancel();
        }
    }

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void folio$frozenNoHotbarScroll(
        final long handle, final double xoffset, final double yoffset, final CallbackInfo ci
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gui.screen() == null && minecraft.player != null && Frozen.is(minecraft.player)) {
            ci.cancel();
        }
    }
}
