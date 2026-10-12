package com.aura.ambrosia.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SoulPepperItem extends Item {
    public static final float FIRE_SECONDS = 5.0F;

    public SoulPepperItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        entity.igniteForSeconds(FIRE_SECONDS);
        return result;
    }
}
