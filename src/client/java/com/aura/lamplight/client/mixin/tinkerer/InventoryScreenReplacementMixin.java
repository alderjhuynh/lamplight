package com.aura.lamplight.client.mixin.tinkerer;

import com.aura.lamplight.tinkerer.client.CharmDeckScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Minecraft.class)
public abstract class InventoryScreenReplacementMixin {

	@Redirect(
			method = "handleKeybinds",
			at = @At(value = "NEW",
					target = "(Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/client/gui/screens/inventory/InventoryScreen;"))
	private InventoryScreen lamplight$openCharmDeck(Player player) {
		return new CharmDeckScreen(player);
	}
}
