package com.aura.lamplight.mixin.armory;

import com.aura.lamplight.armory.DualWielded;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class SlotDualWieldMixin {
	@Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
	private void armory$guardDualWieldPlacement(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (DualWielded.isGhost(stack)) {
			cir.setReturnValue(false);
			return;
		}

		Slot self = (Slot) (Object) this;
		if (self.container instanceof Inventory inventory
				&& self.getContainerSlot() == Inventory.SLOT_OFFHAND
				&& (DualWielded.isDualWielded(stack) || DualWielded.isActive(inventory.player))) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
	private void armory$guardGhostPickup(Player player, CallbackInfoReturnable<Boolean> cir) {
		Slot self = (Slot) (Object) this;
		if (DualWielded.isGhost(self.getItem())) {
			cir.setReturnValue(false);
		}
	}
}
