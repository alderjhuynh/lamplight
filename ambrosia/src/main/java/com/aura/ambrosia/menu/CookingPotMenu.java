package com.aura.ambrosia.menu;

import com.aura.ambrosia.block.CookingPotBlockEntity;
import com.aura.ambrosia.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class CookingPotMenu extends AbstractContainerMenu {
    public static final int INPUT_COUNT = 6;
    public static final int CONTAINER_INDEX = 6;
    public static final int OUTPUT_INDEX = 7;
    private static final int GHOST_INDEX = 8;
    private static final int INV_START = 9;
    private static final int INV_END = 36;
    private static final int HOTBAR_END = 45;

    private static final int[][] INPUT_POS = {{29, 16}, {47, 16}, {65, 16}, {29, 34}, {47, 34}, {65, 34}};

    private final Container pot;
    private final ContainerData data;
    private final SimpleContainer ghost = new SimpleContainer(1);
    private final Level level;
    private final BlockPos pos;

    /** Client constructor: placeholder storage, synced from the server. */
    public CookingPotMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        this(containerId, playerInventory, new SimpleContainer(CookingPotBlockEntity.SLOT_COUNT),
                new SimpleContainerData(3), playerInventory.player.level(), pos);
    }

    /** Server constructor. */
    public CookingPotMenu(int containerId, Inventory playerInventory, CookingPotBlockEntity pot, ContainerData data) {
        this(containerId, playerInventory, pot, data, pot.getLevel(), pot.getBlockPos());
    }

    private CookingPotMenu(int containerId, Inventory playerInventory, Container pot, ContainerData data, Level level, BlockPos pos) {
        super(ModMenus.COOKING_POT, containerId);
        this.pot = pot;
        this.data = data;
        this.level = level;
        this.pos = pos;
        checkContainerSize(pot, CookingPotBlockEntity.SLOT_COUNT);
        checkContainerDataCount(data, 3);
        pot.startOpen(playerInventory.player);
        for (int i = 0; i < INPUT_COUNT; i++) {
            this.addSlot(new Slot(pot, i, INPUT_POS[i][0], INPUT_POS[i][1]));
        }
        this.addSlot(new Slot(pot, CONTAINER_INDEX, 91, 54) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.BOWL);
            }
        });
        this.addSlot(new Slot(pot, OUTPUT_INDEX, 123, 54) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new Slot(this.ghost, 0, 123, 20) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player player) {
                return false;
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 7 + col * 18, 83 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 7 + col * 18, 141));
        }
        this.addDataSlots(data);
        this.refreshGhost();
    }

    public float getCookProgress() {
        int total = this.data.get(1);
        return total <= 0 ? 0.0F : Math.min(1.0F, this.data.get(0) / (float) total);
    }

    public boolean isHeated() {
        return this.data.get(2) != 0;
    }

    private void refreshGhost() {
        if (this.level == null || this.level.isClientSide()) {
            return;
        }
        if (this.pot instanceof CookingPotBlockEntity pot) {
            this.ghost.setItem(0, pot.previewResult());
        }
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        this.refreshGhost();
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.level == null) {
            return false;
        }
        if (!(this.level.getBlockState(this.pos).is(ModBlocks.COOKING_POT))) {
            return false;
        }
        return player.distanceToSqr(this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5) <= 64.0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack moved = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem() || index == GHOST_INDEX) {
            return moved;
        }
        ItemStack stack = slot.getItem();
        moved = stack.copy();
        if (index == OUTPUT_INDEX) {
            if (!this.moveItemStackTo(stack, INV_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, moved);
        } else if (index < INV_START) {
            if (!this.moveItemStackTo(stack, INV_START, HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.is(Items.BOWL)) {
            if (!this.moveItemStackTo(stack, CONTAINER_INDEX, CONTAINER_INDEX + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, 0, INPUT_COUNT, false)) {
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
        this.pot.stopOpen(player);
    }
}
