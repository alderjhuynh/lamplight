package com.aura.lamplight.wayfarer;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public class BackpackMenu extends AbstractContainerMenu {

    public static final int SLOTS = 9;

    private static final int GRID_LEFT = 62;
    private static final int GRID_TOP = 52;
    private static final int INVENTORY_TOP = 110;

    private final ItemStack backpackStack;
    private final BackpackContainer contents;

    public BackpackMenu(int containerId, Inventory playerInventory, int backpackSlotIndex) {
        super(ModMenus.LEATHER_BACKPACK, containerId);
        this.backpackStack = playerInventory.getItem(backpackSlotIndex);
        NonNullList<ItemStack> initial = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
        this.backpackStack.getOrDefault(ModDataComponents.BACKPACK_CONTENTS, ItemContainerContents.EMPTY).copyInto(initial);
        this.contents = new BackpackContainer(initial);
        for (int i = 0; i < SLOTS; i++) {
            this.addSlot(new BackpackSlot(this.contents, i,
                    GRID_LEFT + (i % 3) * 18, GRID_TOP + (i / 3) * 18));
        }
        this.addStandardInventorySlots(playerInventory, 8, INVENTORY_TOP);
    }

    public boolean isBackpack(ItemStack stack) {
        return this.backpackStack == stack;
    }

    void writeBack() {
        if (this.backpackStack.isEmpty()) {
            return;
        }
        List<ItemStack> items = new ArrayList<>(SLOTS);
        for (int i = 0; i < SLOTS; i++) {
            items.add(this.contents.getItem(i));
        }
        this.backpackStack.set(ModDataComponents.BACKPACK_CONTENTS, ItemContainerContents.fromItems(items));
    }

    @Override
    public boolean stillValid(Player player) {
        Inventory inventory = player.getInventory();
        for (int i = 0; i < Inventory.INVENTORY_SIZE; i++) {
            if (inventory.getItem(i) == this.backpackStack) {
                return true;
            }
        }
        return inventory.getItem(Inventory.SLOT_OFFHAND) == this.backpackStack;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack moved = stack.copy();
        if (index < SLOTS) {
            if (!this.moveItemStackTo(stack, SLOTS, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, 0, SLOTS, false)) {
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
    public void removed(Player player) {
        super.removed(player);
        this.writeBack();
        if (!player.level().isClientSide()) {
            player.level().playSound(null, player,
                    WayfarerSounds.BACKPACK_CLOSE, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    private final class BackpackContainer extends SimpleContainer {
        private BackpackContainer(NonNullList<ItemStack> items) {
            super(items.toArray(new ItemStack[0]));
        }

        @Override
        public void setChanged() {
            super.setChanged();
            BackpackMenu.this.writeBack();
        }
    }

    private static final class BackpackSlot extends Slot {
        private BackpackSlot(BackpackContainer container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return !stack.is(ModItems.LEATHER_BACKPACK);
        }
    }
}
