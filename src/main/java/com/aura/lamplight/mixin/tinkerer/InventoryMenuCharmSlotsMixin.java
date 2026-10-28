package com.aura.lamplight.mixin.tinkerer;

import com.aura.lamplight.tinkerer.CharmEquipSlot;
import com.aura.lamplight.tinkerer.CharmSlotContainer;
import com.aura.lamplight.tinkerer.charm.CharmDeckLayout;
import com.aura.lamplight.tinkerer.charm.CharmSlotAttributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

import com.aura.lamplight.mixin.armory.AbstractContainerMenuInvoker;

@Mixin(InventoryMenu.class)
public abstract class InventoryMenuCharmSlotsMixin {
	@Shadow
	@Final
	private Player owner;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void lamplight$addCharmSlots(Inventory inventory, boolean active, Player player, CallbackInfo ci) {
		boolean clientSide = this.owner.level().isClientSide();
		Map<String, Integer> capacities = clientSide
				? CharmDeckLayout.clientCapacities()
				: CharmSlotAttributes.capacities(this.owner);
		Map<String, Integer> acquired = clientSide
				? CharmDeckLayout.clientAcquired()
				: CharmSlotAttributes.acquired(this.owner);
		java.util.List<CharmDeckLayout.Well> wells = CharmDeckLayout.wells(this.owner, capacities, acquired);
		for (int i = 0; i < wells.size(); i++) {
			CharmDeckLayout.Well well = wells.get(i);
			CharmSlotContainer container = new CharmSlotContainer(this.owner, well.id());
			((AbstractContainerMenuInvoker) (Object) this).armory$invokeAddSlot(new CharmEquipSlot(
					this.owner, container,
					CharmEquipSlot.DECK_ORIGIN_X + CharmDeckLayout.WELL_X,
					CharmEquipSlot.DECK_ORIGIN_Y + CharmDeckLayout.wellY(i),
					well.def(), well.slug()));
		}
	}
}
