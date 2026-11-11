package com.aura.lamplight.mixin.armory;

import com.aura.lamplight.armory.TwoHanded;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// two handed items cannot be moved into the player's offhand slot through
// any container menu: covers clicks, shift-clicks and click-swaps, since all
// of them are guarded by mayPlace.
@Mixin(Slot.class)
public abstract class SlotTwoHandedMixin {
	@Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
	private void armory$noTwoHandedInOffhandSlot(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		Slot self = (Slot) (Object) this;
		if (self.container instanceof Inventory
				&& self.getContainerSlot() == Inventory.SLOT_OFFHAND
				&& TwoHanded.isTwoHanded(stack)) {
			cir.setReturnValue(false);
		}
	}
}
