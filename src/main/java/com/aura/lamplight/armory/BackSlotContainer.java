package com.aura.lamplight.armory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

// single slot container that reads/writes the player's back slot
// attachment (letting a vanilla slot participate in menu clicks
// while persist + sync are handled by backslotattachments class)
public final class BackSlotContainer implements Container {
	private final Player player;

	public BackSlotContainer(Player player) {
		this.player = player;
	}

	@Override
	public int getContainerSize() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return getItem(0).isEmpty();
	}

	@Override
	public ItemStack getItem(int slot) {
		if (slot != 0) {
			return ItemStack.EMPTY;
		}
		return BackSlotAttachments.getBackStack(player);
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		if (slot != 0 || amount <= 0) {
			return ItemStack.EMPTY;
		}
		ItemStack current = getItem(0).copy();
		if (current.isEmpty()) {
			return ItemStack.EMPTY;
		}
		ItemStack taken = current.split(amount);
		BackSlotAttachments.setBackStack(player, current);
		setChanged();
		return taken;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		if (slot != 0) {
			return ItemStack.EMPTY;
		}
		ItemStack current = getItem(0).copy();
		BackSlotAttachments.setBackStack(player, ItemStack.EMPTY);
		return current;
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		if (slot != 0) {
			return;
		}
		if (stack.isEmpty()) {
			BackSlotAttachments.setBackStack(player, ItemStack.EMPTY);
		} else {
			BackSlotAttachments.setBackStack(player, stack);
		}
		setChanged();
	}

	@Override
	public void setChanged() {
		// re-set the attachment so fabric's sync observes the mutation
		// otherwise it would stay client/server local
		ItemStack current = BackSlotAttachments.getBackStack(player);
		player.setAttached(BackSlotAttachments.BACK_STACK, current.isEmpty() ? ItemStack.EMPTY : current);
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	@Override
	public void clearContent() {
		BackSlotAttachments.setBackStack(player, ItemStack.EMPTY);
	}
}
