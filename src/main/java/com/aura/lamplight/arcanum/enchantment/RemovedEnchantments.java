package com.aura.lamplight.arcanum.enchantment;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.Set;

public final class RemovedEnchantments {
    private RemovedEnchantments() {}

    public static final Set<ResourceKey<Enchantment>> REMOVED = Set.of(
            Enchantments.BINDING_CURSE,
            Enchantments.VANISHING_CURSE,
            Enchantments.DENSITY,
            Enchantments.DEPTH_STRIDER,
            Enchantments.FROST_WALKER,
            Enchantments.IMPALING,
            Enchantments.KNOCKBACK,
            Enchantments.LOYALTY,
            Enchantments.LUCK_OF_THE_SEA,
            Enchantments.MENDING,
            Enchantments.PUNCH,
            Enchantments.RESPIRATION,
            Enchantments.SHARPNESS,
            Enchantments.SWEEPING_EDGE,
            Enchantments.UNBREAKING
    );

    public static boolean isRemoved(Holder<Enchantment> holder) {
        return holder.unwrapKey().map(REMOVED::contains).orElse(false);
    }

    public static boolean isRemoved(ResourceKey<Enchantment> key) {
        return REMOVED.contains(key);
    }
}
