package com.aura.lamplight.tinkerer;

import com.aura.lamplight.tinkerer.charm.CharmAttachments;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class CharmSlotContainer implements Container {

	private final Player player;
	private final Identifier slotId;

	public CharmSlotContainer(final Player player, final Identifier slotId) {
		this.player = player;
		this.slotId = slotId;
	}

	public Player player() {
		return this.player;
	}

	public Identifier slotId() {
		return this.slotId;
	}

	@Override
	public int getContainerSize() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return this.getItem(0).isEmpty();
	}

	@Override
	public ItemStack getItem(final int slot) {
		if (slot != 0) {
			return ItemStack.EMPTY;
		}
		return CharmAttachments.charmIn(this.player, this.slotId);
	}

	@Override
	public ItemStack removeItem(final int slot, final int amount) {
		if (slot != 0 || amount <= 0) {
			return ItemStack.EMPTY;
		}
		ItemStack current = this.getItem(0).copy();
		if (current.isEmpty()) {
			return ItemStack.EMPTY;
		}
		ItemStack taken = current.split(amount);
		CharmAttachments.setCharm(this.player, this.slotId, current);
		this.setChanged();
		return taken;
	}

	@Override
	public ItemStack removeItemNoUpdate(final int slot) {
		if (slot != 0) {
			return ItemStack.EMPTY;
		}
		ItemStack current = this.getItem(0).copy();
		CharmAttachments.setCharm(this.player, this.slotId, ItemStack.EMPTY);
		this.setChanged();
		return current;
	}

	@Override
	public void setItem(final int slot, final ItemStack stack) {
		if (slot != 0) {
			return;
		}
		CharmAttachments.setCharm(this.player, this.slotId, stack.isEmpty() ? ItemStack.EMPTY : stack);
		this.setChanged();
	}

	@Override
	public void setChanged() {
	}

	@Override
	public boolean stillValid(final Player p) {
		return true;
	}

	@Override
	public void clearContent() {
		CharmAttachments.setCharm(this.player, this.slotId, ItemStack.EMPTY);
	}
}
