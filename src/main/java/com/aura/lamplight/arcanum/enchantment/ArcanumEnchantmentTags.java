package com.aura.lamplight.arcanum.enchantment;

import com.aura.lamplight.arcanum.Arcanum;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;

public final class ArcanumEnchantmentTags {
    public static final TagKey<Enchantment> CAPSTONE = create("capstone");

    private ArcanumEnchantmentTags() {}

    public static TagKey<Enchantment> create(String path) {
        return TagKey.create(Registries.ENCHANTMENT, Arcanum.id(path));
    }

    public static boolean isCapstone(Holder<Enchantment> holder) {
        return holder.is(CAPSTONE);
    }
}
