package com.aura.lamplight.tinkerer;

import com.aura.lamplight.tinkerer.charm.CharmAttachments;
import com.aura.lamplight.tinkerer.charm.CharmEvents;
import com.aura.lamplight.tinkerer.charm.CharmSlotTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import org.jspecify.annotations.Nullable;

public class CharmEquipSlot extends Slot {

	public static final int DECK_ORIGIN_X = 178;
	public static final int DECK_ORIGIN_Y = 0;

	private final Player owner;
	private final String slug;

	private final CharmSlotTypes.@Nullable CharmSlotType def;

	private boolean paneVisible = true;

	public CharmEquipSlot(final Player owner, final CharmSlotContainer container, final int x, final int y,
	                      final CharmSlotTypes.@Nullable CharmSlotType def, final String slug) {
		super(container, 0, x, y);
		this.owner = owner;
		this.def = def;
		this.slug = slug;
	}

	public Player getOwner() {
		return this.owner;
	}

	public Identifier slotId() {
		return ((CharmSlotContainer) this.container).slotId();
	}

	public String typeSlug() {
		return this.slug;
	}

	public boolean isGranted() {
		return this.def == null;
	}

	public CharmSlotTypes.@Nullable CharmSlotType definition() {
		return this.def;
	}

	public boolean isLocked() {
		return this.def != null && !CharmAttachments.isUnlocked(this.owner, this.def);
	}

	public boolean tryUnlock() {
		if (!this.isLocked() || !(this.owner instanceof net.minecraft.server.level.ServerPlayer server)) {
			return false;
		}
		return CharmEvents.tryUnlockSlot(server, this.def);
	}

	@Override
	public boolean mayPlace(final ItemStack stack) {
		if (this.isLocked() || !(stack.getItem() instanceof CharmItem charm)) {
			return false;
		}
		return this.slug.equals(charm.slotType());
	}

	@Override
	public boolean mayPickup(final Player player) {
		return !this.isLocked();
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}

	public void setPaneVisible(final boolean visible) {
		this.paneVisible = visible;
	}

	@Override
	public boolean isActive() {
		return !this.owner.level().isClientSide() || this.paneVisible;
	}

	@Override
	public Identifier getNoItemIcon() {
		return Tinkerer.id("container/slot/charm_" + this.slug);
	}
}
