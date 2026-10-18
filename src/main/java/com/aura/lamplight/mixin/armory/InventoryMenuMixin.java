package com.aura.lamplight.mixin.armory;

import com.aura.lamplight.armory.BackSlot;
import com.aura.lamplight.armory.BackSlotContainer;
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
public abstract class InventoryMenuMixin {
	@Shadow
	@Final
	private Player owner;

	// adds back slot directly above vanilla offhand slot.
	@Inject(method = "<init>", at = @At("TAIL"))
	private void armory$addBackSlot(Inventory inventory, boolean active, Player player, CallbackInfo ci) {
		BackSlotContainer container = new BackSlotContainer(owner);
		((AbstractContainerMenuInvoker) (Object) this).armory$invokeAddSlot(new BackSlot(owner, container, 0, BackSlot.X, BackSlot.Y));
	}
}
