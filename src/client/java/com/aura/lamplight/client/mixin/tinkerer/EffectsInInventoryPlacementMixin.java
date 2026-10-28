package com.aura.lamplight.client.mixin.tinkerer;

import com.aura.lamplight.tinkerer.client.CharmDeckScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EffectsInInventory.class)
public abstract class EffectsInInventoryPlacementMixin {

	@Shadow
	@Final
	private AbstractContainerScreen<?> screen;

	@ModifyVariable(method = "canSeeEffects()Z",
			at = @At(value = "STORE", ordinal = 0), ordinal = 0)
	private int lamplight$placePaneRightOfDeckInViewCheck(int x) {
		return placePaneRightOfDeck(x);
	}

	@ModifyVariable(
			method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;II)V",
			at = @At(value = "STORE", ordinal = 0), ordinal = 2)
	private int lamplight$placePaneRightOfDeckInRender(int x) {
		return placePaneRightOfDeck(x);
	}

	private int placePaneRightOfDeck(int x) {
		if (screen instanceof CharmDeckScreen deck && CharmDeckScreen.isDeckOpen()) {
			return CharmDeckScreen.effectsPaneAnchorX(deck);
		}
		return x;
	}
}
