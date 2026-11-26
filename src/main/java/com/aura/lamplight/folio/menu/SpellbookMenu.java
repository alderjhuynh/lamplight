package com.aura.lamplight.folio.menu;

import com.aura.lamplight.folio.item.SpellbookItem;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class SpellbookMenu extends AbstractContainerMenu {

    public static final int EQUIPPED_TARGET = -1;
    public static final int COLUMNS = 3;
    public static final int INVENTORY_TOP = 84;

    private final Player player;
    private final int targetSlot;
    private final SpellbookItem.@Nullable Equipped equipped;
    private final @Nullable ItemStack book;
    private final SpellbookContainer container;

    public SpellbookMenu(final int containerId, final Inventory playerInventory, final int targetSlot) {
        super(FolioMenus.SPELLBOOK, containerId);
        this.player = playerInventory.player;
        this.targetSlot = targetSlot;
        this.equipped = targetSlot == EQUIPPED_TARGET ? SpellbookItem.findEquipped(this.player) : null;
        this.book = this.resolveBook(playerInventory);
        this.container = new SpellbookContainer(this.player, this.book, this::syncTarget);

        int slots = this.container.capacity();
        for (int i = 0; i < slots; i++) {
            this.addSlot(new SpellbookSlot(this.container, i, spellSlotX(i), spellSlotY(i)));
        }
        this.addStandardInventorySlots(playerInventory, 8, INVENTORY_TOP);
    }

    private @Nullable ItemStack resolveBook(final Inventory inventory) {
        if (this.targetSlot == EQUIPPED_TARGET) {
            return this.equipped == null ? null : this.equipped.stack();
        }
        ItemStack stack = inventory.getItem(this.targetSlot);
        return stack.getItem() instanceof SpellbookItem ? stack : null;
    }

    private void syncTarget() {
        if (this.player.level().isClientSide()) {
            return;
        }
        if (this.equipped != null) {
            this.equipped.sync(this.player);
        }
        // Inventory-target books live in this menu's player inventory slots, so
        // the normal container sync sends their updated data automatically.
    }

    public static int spellSlotX(final int index) {
        return 44 + (index % COLUMNS) * 24;
    }

    public static int spellSlotY(final int index) {
        return 22 + (index / COLUMNS) * 24;
    }

    public SpellbookContainer container() {
        return this.container;
    }

    public SpellbookItem.@Nullable Equipped equipped() {
        return this.equipped;
    }

    public int spellSlots() {
        return this.container.capacity();
    }

    public @Nullable ItemStack book() {
        return this.book;
    }

    public int selected() {
        return this.book == null ? -1 : SpellbookItem.getSelected(this.book);
    }

    public boolean hasSpells() {
        return this.book != null && !SpellbookItem.getSpells(this.book).isEmpty();
    }

    public void select(final int index) {
        if (this.book == null) {
            return;
        }
        SpellbookItem.setSelected(this.book, index);
        this.syncTarget();
    }

    @Override
    public boolean stillValid(final Player player) {
        if (this.book == null) {
            return false;
        }
        if (this.targetSlot == EQUIPPED_TARGET) {
            SpellbookItem.Equipped current = SpellbookItem.findEquipped(player);
            return current != null && current.stack() == this.book;
        }
        Inventory inventory = player.getInventory();
        for (int i = 0; i < Inventory.INVENTORY_SIZE; i++) {
            if (inventory.getItem(i) == this.book) {
                return true;
            }
        }
        return inventory.getItem(Inventory.SLOT_OFFHAND) == this.book;
    }

    @Override
    public ItemStack quickMoveStack(final Player player, final int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack moved = stack.copy();
        int spellSlots = this.spellSlots();

        if (index < spellSlots) {
            if (!this.moveItemStackTo(stack, spellSlots, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, 0, spellSlots, false)) {
            return ItemStack.EMPTY;
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
    public void removed(final Player player) {
        super.removed(player);
        this.container.writeBack();
    }

    private static final class SpellbookSlot extends Slot {

        private SpellbookSlot(final SpellbookContainer container, final int index, final int x, final int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(final ItemStack stack) {
            return SpellbookItem.spellOfScroll(stack) != null;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public Identifier getNoItemIcon() {
            return com.aura.lamplight.folio.Folio.id("container/slot/spell_slot");
        }
    }
}
