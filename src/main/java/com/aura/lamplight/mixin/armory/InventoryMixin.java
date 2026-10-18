package com.aura.lamplight.mixin.armory;

import com.aura.lamplight.armory.BackSlotAttachments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


// virtual hand for the back slot (unsheath without removing.)
// while holding flag is set, getSelectedItem resolves to back-slot stack.
// since playerequipment resolves the mainhand through here, attack, use,
// mining speed, and hand renderers operate via the back item. 
@Mixin(Inventory.class)
public abstract class InventoryMixin {
	@Shadow
	@Final
	public Player player;

	@Inject(method = "getSelectedItem", at = @At("HEAD"), cancellable = true)
	private void armory$virtualHand(CallbackInfoReturnable<ItemStack> cir) {
		if (!BackSlotAttachments.isHolding(this.player)) {
			return;
		}
		ItemStack back = BackSlotAttachments.getBackStack(this.player);
		if (back.isEmpty()) {
			// Broke, consumed, dropped, or moved away: sheathe quietly.
			BackSlotAttachments.setHolding(this.player, false);
			return;
		}
		cir.setReturnValue(back);
	}

	// while held, every mainhand-equipment write is directed at the virtual hand
	@Inject(method = "setSelectedItem", at = @At("HEAD"), cancellable = true)
	private void armory$protectParkedHand(ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
		if (!BackSlotAttachments.isHolding(this.player)) {
			return;
		}
		if (BackSlotAttachments.getBackStack(this.player).isEmpty()) {
			BackSlotAttachments.setHolding(this.player, false);
			return;
		}
		Inventory self = (Inventory) (Object) this;
		cir.setReturnValue(self.getItem(self.getSelectedSlot()));
	}

	// ticks the wielded back item as the held item
	@Inject(method = "tick", at = @At("TAIL"))
	private void armory$tickHeldBackWeapon(CallbackInfo ci) {
		if (!BackSlotAttachments.isHolding(this.player)) {
			return;
		}
		ItemStack back = BackSlotAttachments.getBackStack(this.player);
		if (back.isEmpty()) {
			BackSlotAttachments.setHolding(this.player, false);
			return;
		}
		back.inventoryTick(this.player.level(), this.player, EquipmentSlot.MAINHAND);
	}

	@Inject(method = "pickSlot", at = @At("HEAD"))
	private void armory$disarmOnHotbarSwap(int slot, CallbackInfo ci) {
		BackSlotAttachments.setHolding(this.player, false);
	}

	@Inject(method = "addAndPickItem", at = @At("HEAD"))
	private void armory$disarmOnPickBlock(ItemStack stack, CallbackInfo ci) {
		BackSlotAttachments.setHolding(this.player, false);
	}
}
