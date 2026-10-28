package com.aura.lamplight.tinkerer;

import com.aura.lamplight.tinkerer.charm.CharmMaterials;
import com.aura.lamplight.tinkerer.charm.CharmTemplate;
import com.aura.lamplight.tinkerer.charm.CharmTemplates;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import org.jspecify.annotations.Nullable;

public class JewelingTableMenu extends AbstractContainerMenu {

	private static final int SETTING_X = 55;
	private static final int SETTING_Y = 31;
	private static final int INSET_FIRST_X = 87;
	private static final int INSET_FIRST_Y = 31;
	private static final int INSET_STEP = 22;
	private static final int INSET_COLUMNS = 2;
	private static final int INVENTORY_TOP_Y = 84;

	public static final int MAX_INSETS = Math.max(1,
			((INVENTORY_TOP_Y - INSET_FIRST_Y - (INSET_STEP - 3)) / INSET_STEP + 1) * INSET_COLUMNS);

	public static final int SETTING_INDEX = 0;
	public static final int INSET_START = 1;
	public static final int WORKSPACE_SIZE = 1 + MAX_INSETS;
	public static final int RESULT_INDEX = WORKSPACE_SIZE;

	private static final int RESULT_X = 148;
	private static final int RESULT_Y = 30;

	public static int insetX(final int index) {
		return INSET_FIRST_X + (index % INSET_COLUMNS) * INSET_STEP;
	}

	public static int insetY(final int index) {
		return INSET_FIRST_Y + (index / INSET_COLUMNS) * INSET_STEP;
	}

	private final net.minecraft.world.level.Level level;
	private final BlockPos pos;
	private final Player owner;
	private final JewelingSlots crafting;
	private final Container result;
	@Nullable
	private Identifier patternId;

	public JewelingTableMenu(final int containerId, final Inventory playerInventory, final BlockPos pos) {
		super(ModMenus.JEWELING_TABLE, containerId);
		this.level = playerInventory.player.level();
		this.pos = pos;
		this.owner = playerInventory.player;
		this.crafting = new JewelingSlots(this, WORKSPACE_SIZE);
		this.result = new SimpleContainer(1);
		for (int i = 0; i < WORKSPACE_SIZE; i++) {
			int x = i == SETTING_INDEX ? SETTING_X : insetX(i - INSET_START);
			int y = i == SETTING_INDEX ? SETTING_Y : insetY(i - INSET_START);
			this.addSlot(new WorkspaceSlot(this.crafting, i, x, y));
		}
		this.addSlot(new CraftingResultSlot(this.result, 0, RESULT_X, RESULT_Y));
		this.addStandardInventorySlots(playerInventory, 38, INVENTORY_TOP_Y);
	}

	@Override
	public boolean stillValid(final Player player) {
		if (!this.level.getBlockState(this.pos).is(ModBlocks.JEWELING_TABLE)) {
			return false;
		}
		return player.distanceToSqr(this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5) <= 64.0;
	}

	public @Nullable CharmTemplate pattern() {
		return this.patternId == null ? null : CharmTemplates.byId(this.patternId);
	}

	public @Nullable Identifier patternId() {
		return this.patternId;
	}

	public void setPattern(@Nullable final Identifier nextId) {
		this.patternId = nextId;
		CharmTemplate next = nextId == null ? null : CharmTemplates.byId(nextId);
		int usableInsets = next == null ? 0 : Math.min(MAX_INSETS, next.insetCount());
		int[] cells = new int[WORKSPACE_SIZE];
		for (int i = 0; i < WORKSPACE_SIZE; i++) {
			cells[i] = i == SETTING_INDEX ? (next == null ? 0 : 1) : (i - INSET_START < usableInsets ? 1 : 0);
		}
		for (int i = 0; i < WORKSPACE_SIZE; i++) {
			Slot slot = this.slots.get(i);
			if (cells[i] == 0 && slot.hasItem()) {
				ItemStack stranded = slot.getItem();
				slot.setByPlayer(ItemStack.EMPTY);
				this.owner.addItem(stranded);
			}
		}
		for (int i = 0; i < WORKSPACE_SIZE; i++) {
			((WorkspaceSlot) this.slots.get(i)).setActive(cells[i] == 1);
		}
		this.refreshOutput();
	}

	public boolean maySelect(@Nullable final CharmTemplate template) {
		return template != null && CharmTemplates.isAvailable(this.owner, template);
	}

	public void refreshOutput() {
		if (this.slots.size() <= RESULT_INDEX) {
			return;
		}
		this.result.setItem(0, CharmAssembler.assemble(this.crafting, this.pattern()));
	}

	@Override
	public void slotsChanged(final Container container) {
		super.slotsChanged(container);
		this.refreshOutput();
	}

	@Override
	public ItemStack quickMoveStack(final Player player, final int index) {
		Slot slot = this.slots.get(index);
		if (slot == null || !slot.hasItem()) {
			return ItemStack.EMPTY;
		}
		ItemStack stack = slot.getItem();
		ItemStack moved = stack.copy();
		if (index == RESULT_INDEX) {
			if (!this.moveItemStackTo(stack, RESULT_INDEX + 1, this.slots.size(), true)) {
				return ItemStack.EMPTY;
			}
			slot.onQuickCraft(stack, moved);
		} else if (index < RESULT_INDEX) {
			if (!this.moveItemStackTo(stack, RESULT_INDEX + 1, this.slots.size(), false)) {
				return ItemStack.EMPTY;
			}
		} else {
			if (!this.moveItemStackTo(stack, 0, RESULT_INDEX, false)) {
				return ItemStack.EMPTY;
			}
		}
		if (stack.isEmpty()) {
			slot.setByPlayer(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}
		if (stack.getCount() == moved.getCount()) {
			return ItemStack.EMPTY;
		}
		slot.onTake(player, stack);
		return moved;
	}

	@Override
	public boolean canTakeItemForPickAll(final ItemStack carried, final Slot target) {
		return target.container != this.result && super.canTakeItemForPickAll(carried, target);
	}

	@Override
	public void removed(final Player player) {
		super.removed(player);
		if (!player.level().isClientSide()) {
			this.crafting.returnContentsTo(player);
		}
	}

	static final class WorkspaceSlot extends Slot {
		private boolean active;

		private WorkspaceSlot(final JewelingSlots container, final int index, final int x, final int y) {
			super(container, index, x, y);
			this.active = false;
		}

		void setActive(final boolean active) {
			this.active = active;
		}

		@Override
		public boolean isActive() {
			return this.active;
		}

		@Override
		public boolean mayPlace(final ItemStack stack) {
			return this.active && mayBeMaterial(stack);
		}

		@Override
		public boolean mayPickup(final Player player) {
			return this.active;
		}

		static boolean mayBeMaterial(final ItemStack stack) {
			return CharmMaterials.byItem(stack) != null;
		}
	}

	private final class CraftingResultSlot extends Slot {
		private CraftingResultSlot(final Container result, final int index, final int x, final int y) {
			super(result, index, x, y);
		}

		@Override
		public boolean mayPlace(final ItemStack stack) {
			return false;
		}

		@Override
		public void onTake(final Player taker, final ItemStack taken) {
			CharmAssembler.consume(JewelingTableMenu.this.crafting, JewelingTableMenu.this.pattern());
			JewelingTableMenu.this.refreshOutput();
			JewelingTableMenu.this.level.playSound(null, JewelingTableMenu.this.pos,
					SoundEvents.SMITHING_TABLE_USE, SoundSource.PLAYERS, 1.0F,
					JewelingTableMenu.this.owner.getRandom().nextFloat() * 0.1F + 0.9F);
			super.onTake(taker, taken);
		}
	}
}
