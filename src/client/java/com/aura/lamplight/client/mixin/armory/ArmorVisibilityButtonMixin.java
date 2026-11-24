package com.aura.lamplight.client.mixin.armory;

import com.aura.lamplight.armory.client.ArmorVisibilityButtons;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class ArmorVisibilityButtonMixin {
	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void lamplight$drawArmorVisibilityButtons(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
		ArmorVisibilityButtons.extract((AbstractContainerScreen<?>) (Object) this, graphics, mouseX, mouseY);
	}

	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private void lamplight$handleArmorVisibilityClick(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
		if (ArmorVisibilityButtons.mouseClicked((AbstractContainerScreen<?>) (Object) this, event, doubleClick)) {
			cir.setReturnValue(true);
		}
	}
}
