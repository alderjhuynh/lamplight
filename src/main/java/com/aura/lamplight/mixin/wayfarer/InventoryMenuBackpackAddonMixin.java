package com.aura.lamplight.mixin.wayfarer;

import com.aura.lamplight.mixin.armory.AbstractContainerMenuInvoker;
import com.aura.lamplight.wayfarer.BackpackAddonContainer;
import com.aura.lamplight.wayfarer.BackpackAddonSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryMenu.class)
public abstract class InventoryMenuBackpackAddonMixin {
	@Shadow
	@Final
	private Player owner;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void wayfarer$addBackpackAddonSlots(Inventory inventory, boolean active, Player player, CallbackInfo ci) {
		BackpackAddonContainer container = new BackpackAddonContainer(this.owner);
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) {
				((AbstractContainerMenuInvoker) (Object) this).armory$invokeAddSlot(new BackpackAddonSlot(
						this.owner, container,
						row * 3 + col,
						BackpackAddonSlot.ORIGIN_X + BackpackAddonSlot.GRID_X + col * 18,
						BackpackAddonSlot.ORIGIN_Y + BackpackAddonSlot.GRID_Y + row * 18));
			}
		}
	}
}
