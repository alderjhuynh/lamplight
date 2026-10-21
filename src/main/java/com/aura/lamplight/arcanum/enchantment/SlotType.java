package com.aura.lamplight.arcanum.enchantment;

import com.aura.lamplight.arcanum.Arcanum;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public enum SlotType {
    EDGE("lamplight.slot.edge", "slot/edge"),
    TEMPO("lamplight.slot.tempo", "slot/tempo"),
    FLOURISH("lamplight.slot.flourish", "slot/flourish"),
    WARD("lamplight.slot.ward", "slot/ward"),
    STRIDE("lamplight.slot.stride", "slot/stride"),
    RITE("lamplight.slot.rite", "slot/rite"),
    METHOD("lamplight.slot.method", "slot/method"),
    BOON("lamplight.slot.boon", "slot/boon"),
    MAIN_EFFECT("lamplight.slot.main_effect", "slot/main_effect"),
    SECONDARY_EFFECT("lamplight.slot.secondary_effect", "slot/secondary_effect");

    private final String translationKey;
    private final TagKey<Enchantment> tag;

    SlotType(String translationKey, String tagPath) {
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

    public static List<SlotType> rolesFor(Holder<Enchantment> holder) {
        List<SlotType> roles = new ArrayList<>(1);
        for (SlotType type : values()) {
            if (type.contains(holder)) {
                roles.add(type);
            }
        }
        return List.copyOf(roles);
    }

    public static Optional<SlotType> forEnchantment(Holder<Enchantment> holder) {
        List<SlotType> roles = rolesFor(holder);
        return roles.size() == 1 ? Optional.of(roles.getFirst()) : Optional.empty();
    }
}
