package com.aura.lamplight.client.mixin.armory;

import com.aura.lamplight.armory.client.ArmorVisibilityButtons;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenArmorVisibilityMixin {
	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void lamplight$draw(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
		ArmorVisibilityButtons.extract((InventoryScreen) (Object) this, graphics, mouseX, mouseY);
	}
}
