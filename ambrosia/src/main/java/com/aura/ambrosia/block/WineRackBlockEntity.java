package com.aura.ambrosia.block;

import com.aura.ambrosia.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class WineRackBlockEntity extends BlockEntity implements Container {
    public static final int SLOT_COUNT = 4;
    public static final int WINE_TO_GRAPE_TICKS = 6000;
    public static final int GRAPE_TO_FINE_TICKS = 12000;

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final int[] age = new int[SLOT_COUNT];

    public WineRackBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WINE_RACK, pos, state);
    }

    public boolean insert(ItemStack stack) {
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (this.items.get(i).isEmpty()) {
                this.items.set(i, stack.copyWithCount(1));
                stack.shrink(1);
                this.age[i] = 0;
                this.setChanged();
                return true;
            }
        }
        return false;
    }
    
    public ItemStack take() {
        int best = -1;
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (!this.items.get(i).isEmpty() && (best < 0 || this.age[i] > this.age[best])) {
                best = i;
            }
        }
        if (best < 0) {
            return ItemStack.EMPTY;
        }
        ItemStack result = this.items.get(best).copy();
        this.items.set(best, ItemStack.EMPTY);
        this.age[best] = 0;
        this.setChanged();
        return result;
    }

    public int fillLevel() {
        int filled = 0;
        for (ItemStack stack : this.items) {
            if (!stack.isEmpty()) {
                filled++;
            }
        }
        if (filled == 0) {
            return 0;
        }
        return 1 + (int) ((filled / (float) SLOT_COUNT) * 14.0F);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, WineRackBlockEntity rack) {
        if (level.isClientSide()) {
            return;
        }
        boolean changed = false;
        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack stack = rack.items.get(i);
            if (stack.isEmpty()) {
                continue;
            }
            Item aged = agedResult(stack.getItem(), rack.age[i]);
            if (aged != null) {
                rack.items.set(i, new ItemStack(aged, stack.getCount()));
                rack.age[i] = 0;
                changed = true;
            } else if (rack.age[i] < GRAPE_TO_FINE_TICKS) {
                rack.age[i]++;
                changed = rack.age[i] % 20 == 0;
            }
        }
        if (changed) {
            rack.setChanged();
        }
    }

    private static Item agedResult(Item item, int age) {
        if (item == ModItems.WINE && age >= WINE_TO_GRAPE_TICKS) {
            return ModItems.GRAPE_WINE;
        }
        if (item == ModItems.GRAPE_WINE && age >= GRAPE_TO_FINE_TICKS) {
            return ModItems.FINE_WINE;
        }
        return null;
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int index) {
        return this.items.get(index);
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        ItemStack result = ContainerHelper.removeItem(this.items, index, count);
        if (!result.isEmpty()) {
            this.setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        return ContainerHelper.takeItem(this.items, index);
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        this.items.set(index, stack);
        this.setChanged();
    }

    @Override
    public void setChanged() {
        super.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
        output.putIntArray("Age", this.age);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, this.items);
        int[] loaded = input.getIntArray("Age").orElse(new int[0]);
        System.arraycopy(loaded, 0, this.age, 0, Math.min(loaded.length, this.age.length));
    }
}
