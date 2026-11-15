package com.aura.lamplight.ambrosia.block;

import com.aura.lamplight.ambrosia.item.ModItems;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

public enum PotFluid implements StringRepresentable {
    EMPTY("empty", false, 0xFF000000, null, null),
    WATER("water", true, 0xFF3F76E4, () -> PotionContents.createItemStack(Items.POTION, Potions.WATER), null),
    MILK("milk", false, 0xFFE9F0F4, () -> new ItemStack(ModItems.MILK_BOTTLE), null),
    HONEY("honey", false, 0xFFCFA865, () -> new ItemStack(Items.HONEY_BOTTLE), null),
    COFFEE("coffee", false, 0xFF4E2E17, () -> new ItemStack(ModItems.COFFEE), null),
    HOT_CHOCOLATE("hot_chocolate", false, 0xFF7A4E2B, () -> new ItemStack(ModItems.HOT_CHOCOLATE), null),
    APPLE_CIDER("apple_cider", false, 0xFF9C6B2F, () -> new ItemStack(ModItems.APPLE_CIDER), null),
    WINE("wine", false, 0xFF6E1F2E, () -> new ItemStack(ModItems.WINE), null),
    MULLED_WINE("mulled_wine", false, 0xFF803929, () -> new ItemStack(ModItems.MULLED_WINE), null),
    GRAPE_WINE("grape_wine", false, 0xFF7E2456, () -> new ItemStack(ModItems.GRAPE_WINE), null),
    FINE_WINE("fine_wine", false, 0xFF3D0030, () -> new ItemStack(ModItems.FINE_WINE), null),
    SWEET_BERRY_WINE("sweet_berry_wine", false, 0xFF8E2018, () -> new ItemStack(ModItems.SWEET_BERRY_WINE), null),
    BLAZING_WINE("blazing_wine", false, 0xFFAC4A10, () -> new ItemStack(ModItems.BLAZING_WINE), null),
    BREEZING_WINE("breezing_wine", false, 0xFF4C5E98, () -> new ItemStack(ModItems.BREEZING_WINE), null),
    CREEPING_WINE("creeping_wine", false, 0xFF58322A, () -> new ItemStack(ModItems.CREEPING_WINE), null),
    DIVING_WINE("diving_wine", false, 0xFF163E68, () -> new ItemStack(ModItems.DIVING_WINE), null),
    GLEAMING_WINE("gleaming_wine", false, 0xFFA86410, () -> new ItemStack(ModItems.GLEAMING_WINE), null),
    BLESSING_WINE("blessing_wine", false, 0xFF0F6216, () -> new ItemStack(ModItems.BLESSING_WINE), null),
    CHORUS_WINE("chorus_wine", false, 0xFF703C74, () -> new ItemStack(ModItems.CHORUS_WINE), null),
    HEROIC_WINE("heroic_wine", false, 0xFF409A1A, () -> new ItemStack(ModItems.HEROIC_WINE), null),
    PROSPECTING_WINE("prospecting_wine", false, 0xFF4C2E7E, () -> new ItemStack(ModItems.PROSPECTING_WINE), null),

    MUSHROOM_STEW("mushroom_stew", false, 0xFF895E3E, null, () -> new ItemStack(Items.MUSHROOM_STEW)),
    RABBIT_STEW("rabbit_stew", false, 0xFFBB6C34, null, () -> new ItemStack(Items.RABBIT_STEW)),
    BEETROOT_SOUP("beetroot_soup", false, 0xFF682412, null, () -> new ItemStack(Items.BEETROOT_SOUP)),
    SUSPICIOUS_STEW("suspicious_stew", false, 0xFF87734C, null, () -> new ItemStack(Items.SUSPICIOUS_STEW)),
    STEW("stew", false, 0xFF825122, null, () -> new ItemStack(ModItems.STEW)),
    TOMATO_SOUP("tomato_soup", false, 0xFF894325, null, () -> new ItemStack(ModItems.TOMATO_SOUP)),
    VEGETABLE_SOUP("vegetable_soup", false, 0xFF93623B, null, () -> new ItemStack(ModItems.VEGETABLE_SOUP)),
    MUSHROOM_RISOTTO("mushroom_risotto", false, 0xFFB8AB7B, null, () -> new ItemStack(ModItems.MUSHROOM_RISOTTO)),
    BEEF_STROGANOFF("beef_stroganoff", false, 0xFF976C43, null, () -> new ItemStack(ModItems.BEEF_STROGANOFF)),
    SPAGHETTI("spaghetti", false, 0xFFA9541C, null, () -> new ItemStack(ModItems.SPAGHETTI)),
    EGG_FRIED_RICE("egg_fried_rice", false, 0xFFAB8D3C, null, () -> new ItemStack(ModItems.EGG_FRIED_RICE));

    public static final Codec<PotFluid> CODEC = StringRepresentable.fromValues(PotFluid::values);

    private final String name;
    private final boolean translucent;
    private final int color;
    /** The drink handed back when scooped with an empty glass bottle (drinks only). */
    @Nullable
    private final Supplier<ItemStack> bottleScoop;
    /** The stew handed back when scooped with an empty bowl (soupy fluids only). */
    @Nullable
    private final Supplier<ItemStack> bowlScoop;

    PotFluid(String name, boolean translucent, int color, @Nullable Supplier<ItemStack> bottleScoop, @Nullable Supplier<ItemStack> bowlScoop) {
        this.name = name;
        this.translucent = translucent;
        this.color = color;
        this.bottleScoop = bottleScoop;
        this.bowlScoop = bowlScoop;
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

    @Nullable
    public ItemStack scoopWith(ItemStack container) {
        Supplier<ItemStack> scoop;
        if (container.is(Items.GLASS_BOTTLE)) {
            scoop = this.bottleScoop;
        } else if (container.is(Items.BOWL)) {
            scoop = this.bowlScoop;
        } else {
            return null;
        }
        return scoop == null ? null : scoop.get();
    }

    public static ItemStack pourRemainder(ItemStack stack, PotFluid poured) {
        if (stack.is(Items.WATER_BUCKET) || stack.is(Items.MILK_BUCKET)) {
            return new ItemStack(Items.BUCKET);
        }
        if (poured.bowlScoop != null) {
            return new ItemStack(Items.BOWL);
        }
        return new ItemStack(Items.GLASS_BOTTLE);
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
        if (ModItems.isWine(stack)) {
            String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
            for (PotFluid fluid : values()) {
                if (fluid.name.equals(path)) {
                    return fluid;
                }
            }
        }
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        for (PotFluid fluid : values()) {
            if (fluid.bowlScoop != null && fluid.name.equals(path)) {
                return fluid;
            }
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
