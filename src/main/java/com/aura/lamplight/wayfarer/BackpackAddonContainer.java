package com.aura.lamplight.wayfarer;

import com.aura.lamplight.armory.BackSlotAttachments;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

import org.jspecify.annotations.Nullable;

public final class BackpackAddonContainer implements Container {
	private final Player player;
	private final NonNullList<ItemStack> items = NonNullList.withSize(BackpackMenu.SLOTS, ItemStack.EMPTY);

	@Nullable
	private ItemStack cachedBackpack;

	public BackpackAddonContainer(Player player) {
		this.player = player;
	}

	public boolean hasBackpack() {
		return !BackSlotAttachments.getBackStack(this.player).isEmpty();
	}

	private void refresh() {
		ItemStack backpack = BackSlotAttachments.getBackStack(this.player);
		if (backpack == this.cachedBackpack) {
			return;
		}
		this.cachedBackpack = backpack;
		if (backpack.isEmpty() || !(backpack.getItem() instanceof BackpackItem)) {
			for (int i = 0; i < BackpackMenu.SLOTS; i++) {
				this.items.set(i, ItemStack.EMPTY);
			}
		} else {
			backpack.getOrDefault(ModDataComponents.BACKPACK_CONTENTS, ItemContainerContents.EMPTY)
					.copyInto(this.items);
		}
	}

	private void writeBack() {
		ItemStack backpack = BackSlotAttachments.getBackStack(this.player);
		if (backpack.isEmpty() || !(backpack.getItem() instanceof BackpackItem)) {
			return;
		}
		this.cachedBackpack = backpack;
		backpack.set(ModDataComponents.BACKPACK_CONTENTS, ItemContainerContents.fromItems(this.items));
		this.player.setAttached(BackSlotAttachments.BACK_STACK, backpack);
	}

	@Override
	public int getContainerSize() {
		return BackpackMenu.SLOTS;
	}

	@Override
	public boolean isEmpty() {
		for (int i = 0; i < BackpackMenu.SLOTS; i++) {
			if (!getItem(i).isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack getItem(int slot) {
		if (slot < 0 || slot >= BackpackMenu.SLOTS) {
			return ItemStack.EMPTY;
		}
		this.refresh();
		return this.items.get(slot);
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		this.refresh();
		if (slot < 0 || slot >= BackpackMenu.SLOTS || amount <= 0) {
			return ItemStack.EMPTY;
		}
		ItemStack current = this.items.get(slot).copy();
		if (current.isEmpty()) {
			return ItemStack.EMPTY;
		}
		ItemStack taken = current.split(amount);
		this.items.set(slot, current);
		this.writeBack();
		return taken;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		this.refresh();
		if (slot < 0 || slot >= BackpackMenu.SLOTS) {
			return ItemStack.EMPTY;
		}
		ItemStack current = this.items.get(slot);
		this.items.set(slot, ItemStack.EMPTY);
		this.writeBack();
		return current;
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		this.refresh();
		if (slot < 0 || slot >= BackpackMenu.SLOTS) {
			return;
		}
		this.items.set(slot, stack);
		this.writeBack();
	}

	@Override
	public void setChanged() {
		this.writeBack();
	}

	@Override
	public boolean stillValid(Player viewer) {
		return this.hasBackpack();
	}

	@Override
	public void clearContent() {
		for (int i = 0; i < BackpackMenu.SLOTS; i++) {
			this.items.set(i, ItemStack.EMPTY);
		}
		this.writeBack();
	}
}
