package com.aura.lamplight.ambrosia.block;

import com.aura.lamplight.ambrosia.item.ModItems;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

import java.util.function.Supplier;

public enum PotFluid implements StringRepresentable {
    EMPTY("empty", false, 0xFF000000, () -> ItemStack.EMPTY),
    WATER("water", true, 0xFF3F76E4, () -> PotionContents.createItemStack(Items.POTION, Potions.WATER)),
    MILK("milk", false, 0xFFE9F0F4, () -> new ItemStack(ModItems.MILK_BOTTLE)),
    HONEY("honey", false, 0xFFCFA865, () -> new ItemStack(Items.HONEY_BOTTLE)),
    COFFEE("coffee", false, 0xFF4E2E17, () -> new ItemStack(ModItems.COFFEE)),
    HOT_CHOCOLATE("hot_chocolate", false, 0xFF7A4E2B, () -> new ItemStack(ModItems.HOT_CHOCOLATE)),
    APPLE_CIDER("apple_cider", false, 0xFF9C6B2F, () -> new ItemStack(ModItems.APPLE_CIDER));

    public static final Codec<PotFluid> CODEC = StringRepresentable.fromValues(PotFluid::values);

    private final String name;
    private final boolean translucent;
    private final int color;
    private final Supplier<ItemStack> scoopItem;

    PotFluid(String name, boolean translucent, int color, Supplier<ItemStack> scoopItem) {
        this.name = name;
        this.translucent = translucent;
        this.color = color;
        this.scoopItem = scoopItem;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public boolean isTranslucent() {
        return this.translucent;
    }

    public int color() {
        return this.color;
    }

    public ItemStack scoopItem() {
        return this.scoopItem.get();
    }

    public static ItemStack pourRemainder(ItemStack stack) {
        return stack.is(Items.WATER_BUCKET) || stack.is(Items.MILK_BUCKET)
                ? new ItemStack(Items.BUCKET)
                : new ItemStack(Items.GLASS_BOTTLE);
    }

    public static PotFluid pourOf(ItemStack stack) {
        if (stack.is(Items.WATER_BUCKET) || isWaterBottle(stack)) {
            return WATER;
        }
        if (stack.is(Items.MILK_BUCKET) || stack.is(ModItems.MILK_BOTTLE)) {
            return MILK;
        }
        if (stack.is(Items.HONEY_BOTTLE)) {
            return HONEY;
        }
        if (stack.is(ModItems.COFFEE)) {
            return COFFEE;
        }
        if (stack.is(ModItems.HOT_CHOCOLATE)) {
            return HOT_CHOCOLATE;
        }
        if (stack.is(ModItems.APPLE_CIDER)) {
            return APPLE_CIDER;
        }
        return null;
    }

    private static boolean isWaterBottle(ItemStack stack) {
        if (!stack.is(Items.POTION)) {
            return false;
        }
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        return contents == null || contents == PotionContents.EMPTY || !contents.getAllEffects().iterator().hasNext();
    }
}
