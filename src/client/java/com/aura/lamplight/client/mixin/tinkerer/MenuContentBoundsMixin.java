package com.aura.lamplight.client.mixin.tinkerer;

import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(AbstractContainerMenu.class)
public abstract class MenuContentBoundsMixin {
	@Shadow
	@Final
	public NonNullList<Slot> slots;

	@Shadow
	private ItemStack carried;

	@Shadow
	private int stateId;

	@Inject(method = "initializeContents", at = @At("HEAD"), cancellable = true)
	private void lamplight$boundedInitializeContents(int stateId, List<ItemStack> items, ItemStack carried,
	                                                 CallbackInfo ci) {
		int count = Math.min(items.size(), this.slots.size());
		for (int i = 0; i < count; i++) {
			this.slots.get(i).set(items.get(i));
		}
		this.carried = carried;
		this.stateId = stateId;
		ci.cancel();
	}
}
