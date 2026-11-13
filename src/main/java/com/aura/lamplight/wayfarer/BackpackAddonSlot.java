package com.aura.lamplight.wayfarer;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BackpackAddonSlot extends Slot {

	public static final int ORIGIN_X = -70;
	public static final int ORIGIN_Y = 0;
	public static final int PANEL_WIDTH = 68;
	public static final int PANEL_HEIGHT = 114;
	public static final int PANE_GAP = 2;

	public static final int GRID_X = 8;
	public static final int GRID_Y = 54;

	private final Player owner;
	private boolean paneVisible = true;

	public BackpackAddonSlot(Player owner, BackpackAddonContainer container, int containerSlot, int x, int y) {
		super(container, containerSlot, x, y);
		this.owner = owner;
	}

	public Player getOwner() {
		return this.owner;
	}

	public void setPaneVisible(final boolean visible) {
		this.paneVisible = visible;
	}

	@Override
	public boolean isActive() {
		return !this.owner.level().isClientSide()
				|| (this.paneVisible && ((BackpackAddonContainer) this.container).hasBackpack());
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return ((BackpackAddonContainer) this.container).hasBackpack()
				&& !stack.is(ModItems.LEATHER_BACKPACK);
	}

	@Override
	public boolean mayPickup(Player player) {
		return ((BackpackAddonContainer) this.container).hasBackpack();
	}
}
