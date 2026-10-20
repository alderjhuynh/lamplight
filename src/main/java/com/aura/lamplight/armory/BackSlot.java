package com.aura.lamplight.armory;

import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

// back slot, accepts anything and renders on the player's back. 

public class BackSlot extends Slot {
	public static final int X = 77;
	public static final int Y = 44;

	// the creative inventory lays out the player inventory differently;
	// its back slot sits opposite the offhand, right of the armor slots.
	public static final int CREATIVE_X = 127;
	public static final int CREATIVE_Y = 20;

	// empty state ghost icon
	public static final Identifier EMPTY_ICON = Armory.id("container/slot/back_weapon");

	private final Player owner;

	public BackSlot(Player owner, Container container, int containerSlot, int x, int y) {
		super(container, containerSlot, x, y);
		this.owner = owner;
	}

	public Player getOwner() {
		return owner;
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return true;
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}

	@Override
	public boolean isActive() {
		return true;
	}

	@Override
	public Identifier getNoItemIcon() {
		return EMPTY_ICON;
	}
}
