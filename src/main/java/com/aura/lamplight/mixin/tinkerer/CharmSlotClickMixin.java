package com.aura.lamplight.mixin.tinkerer;

import com.aura.lamplight.tinkerer.CharmEquipSlot;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(AbstractContainerMenu.class)
public abstract class CharmSlotClickMixin {

	@Inject(method = "doClick", at = @At("HEAD"), cancellable = true)
	private void lamplight$charmSlotUnlock(final int slotIndex, final int buttonNum, final ContainerInput input,
	                                       final Player player, final CallbackInfo ci) {
		if (input != ContainerInput.PICKUP) {
			return;
		}
		List<Slot> slots = ((AbstractContainerMenu) (Object) this).slots;
		if (slotIndex < 0 || slotIndex >= slots.size()) {
			return;
		}
		if (!(slots.get(slotIndex) instanceof CharmEquipSlot charmSlot)) {
			return;
		}
		if (!charmSlot.isLocked()) {
			return;
		}
		charmSlot.tryUnlock();
		ci.cancel();
	}
}
