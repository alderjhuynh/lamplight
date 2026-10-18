package com.aura.lamplight.menagerie.crow;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public interface TargetsDroppedItems {
    boolean canTargetItem(ItemStack stack);

    void onGetItem(ItemEntity entity);

    default void onFindTarget(ItemEntity entity) {
    }

    default double getMaxDistToItem() {
        return 2.0D;
    }
}
