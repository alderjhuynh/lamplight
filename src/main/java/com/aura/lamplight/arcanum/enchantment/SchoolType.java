package com.aura.lamplight.arcanum.enchantment;

import com.aura.lamplight.arcanum.Arcanum;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Optional;

// i will probably remove this, i lowkey hate it

public enum SchoolType {
    EMBER("lamplight.school.ember", "school/ember"),
    FROST("lamplight.school.frost", "school/frost"),
    STORM("lamplight.school.storm", "school/storm"),
    IRON("lamplight.school.iron", "school/iron"),
    VERDANT("lamplight.school.verdant", "school/verdant"),
    GALE("lamplight.school.gale", "school/gale"),
    GRAVE("lamplight.school.grave", "school/grave"),
    ARTISAN("lamplight.school.artisan", "school/artisan");

    private final String translationKey;
    private final TagKey<Enchantment> tag;

    SchoolType(String translationKey, String tagPath) {
        this.translationKey = translationKey;
        this.tag = TagKey.create(Registries.ENCHANTMENT, Arcanum.id(tagPath));
    }

    public String translationKey() {
        return translationKey;
    }

    public Component displayName() {
        return Component.translatable(translationKey);
    }

    public TagKey<Enchantment> tag() {
        return tag;
    }

    public boolean contains(Holder<Enchantment> holder) {
        return holder.is(tag);
    }

    public static Optional<SchoolType> forEnchantment(Holder<Enchantment> holder) {
        for (SchoolType type : values()) {
            if (type.contains(holder)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }
}
