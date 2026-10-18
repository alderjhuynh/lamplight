package com.aura.lamplight.client.mixin.ambrosia.appleskin;

import com.aura.lamplight.ambrosia.client.appleskin.TooltipOverlayHandler;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientTooltipComponent.class)
public interface ClientTooltipComponentMixin extends ClientTooltipComponent {

    /**
     * Unwraps the food overlay smuggled in as a text line back into its image
     * form when the tooltip is assembled for rendering.
     */
    @Inject(
            at = @At("HEAD"),
            method = "create(Lnet/minecraft/util/FormattedCharSequence;)Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipComponent;",
            cancellable = true
    )
    private static void ambrosia$unwrapFoodOverlay(FormattedCharSequence text,
                                                   CallbackInfoReturnable<ClientTooltipComponent> cir) {
        if (text instanceof TooltipOverlayHandler.FoodOverlayTextComponent overlayText) {
            cir.setReturnValue(overlayText.foodOverlay);
        }
    }

    @Inject(
            at = @At("HEAD"),
            method = "create(Lnet/minecraft/world/inventory/tooltip/TooltipComponent;)Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipComponent;",
            cancellable = true
    )
    private static void ambrosia$useFoodOverlay(TooltipComponent data,
                                                CallbackInfoReturnable<ClientTooltipComponent> cir) {
        if (data instanceof TooltipOverlayHandler.FoodOverlay overlay) {
            cir.setReturnValue(overlay);
        }
    }
}
