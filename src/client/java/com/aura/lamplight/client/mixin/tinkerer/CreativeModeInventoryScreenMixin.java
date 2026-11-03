package com.aura.lamplight.client.mixin.tinkerer;

import com.aura.lamplight.tinkerer.CharmEquipSlot;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin {
	@ModifyArgs(
			method = "selectTab",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen$SlotWrapper;<init>(Lnet/minecraft/world/inventory/Slot;III)V"
			)
	)
	private void tinkerer$hideCharmDeck(Args args) {
		if (args.get(0) instanceof CharmEquipSlot) {
			args.set(2, -2000);
			args.set(3, -2000);
		}
	}
}
