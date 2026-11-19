package com.aura.lamplight.folio.menu;

import com.aura.lamplight.folio.Folio;
import com.aura.lamplight.folio.item.SpellbookItem;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class SpellbookMenu extends AbstractContainerMenu {

    public static final int COLUMNS = 3;
    public static final int INVENTORY_TOP = 84;

    private final Player player;
    private final SpellbookItem.@Nullable Equipped equipped;
    private final SpellbookContainer container;

    public SpellbookMenu(final int containerId, final Inventory playerInventory) {
        super(FolioMenus.SPELLBOOK, containerId);
        this.player = playerInventory.player;
        this.equipped = SpellbookItem.findEquipped(this.player);
        this.container = new SpellbookContainer(this.player, this.equipped);

        int slots = this.container.capacity();
        for (int i = 0; i < slots; i++) {
            this.addSlot(new SpellbookSlot(this.container, i, spellSlotX(i), spellSlotY(i)));
        }
        this.addStandardInventorySlots(playerInventory, 8, INVENTORY_TOP);
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
        return this.equipped == null ? null : this.equipped.stack();
    }

    public void select(final int index) {
        if (this.equipped == null) {
            return;
        }
        SpellbookItem.setSelected(this.equipped.stack(), index);
        this.equipped.sync(this.player);
    }

    @Override
    public boolean stillValid(final Player player) {
        SpellbookItem.Equipped current = SpellbookItem.findEquipped(player);
        return current != null && this.equipped != null
            && current.slotId().equals(this.equipped.slotId())
            && !current.stack().isEmpty();
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
            return Folio.id("container/slot/spell_slot");
        }
    }
}
