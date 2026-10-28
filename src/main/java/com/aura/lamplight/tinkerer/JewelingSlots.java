package com.aura.lamplight.tinkerer;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public final class JewelingSlots implements Container {

	private final AbstractContainerMenu menu;
	private final ItemStack[] items;

	public JewelingSlots(final AbstractContainerMenu menu, final int size) {
		this.menu = menu;
		this.items = new ItemStack[size];
		java.util.Arrays.fill(this.items, ItemStack.EMPTY);
	}

	public ItemStack stackIn(final int index) {
		return this.items[index];
	}

	public void setStackIn(final int index, final ItemStack stack) {
		this.items[index] = stack;
		this.setChanged();
	}

	public ItemStack settingStack() {
		return this.stackIn(JewelingTableMenu.SETTING_INDEX);
	}

	@Override
	public int getContainerSize() {
		return this.items.length;
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack item : this.items) {
			if (!item.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack getItem(final int slot) {
		return this.items[slot];
	}

	@Override
	public ItemStack removeItem(final int slot, final int amount) {
		ItemStack current = this.items[slot];
		if (current.isEmpty() || amount <= 0) {
			return ItemStack.EMPTY;
		}
		ItemStack taken = current.split(amount);
		this.setChanged();
		return taken;
	}

	@Override
	public ItemStack removeItemNoUpdate(final int slot) {
		ItemStack current = this.items[slot];
		this.items[slot] = ItemStack.EMPTY;
		this.setChanged();
		return current;
	}

	@Override
	public void setItem(final int slot, final ItemStack stack) {
		this.items[slot] = stack.isEmpty() ? ItemStack.EMPTY : stack;
		this.setChanged();
	}

	@Override
	public void setChanged() {
		this.menu.slotsChanged(this);
	}

	@Override
	public boolean stillValid(final Player player) {
		return true;
	}

	@Override
	public void clearContent() {
		java.util.Arrays.fill(this.items, ItemStack.EMPTY);
	}

	public void returnContentsTo(final Player player) {
		for (ItemStack item : this.items) {
			if (!item.isEmpty()) {
				player.addItem(item);
			}
		}
		this.clearContent();
	}
}
