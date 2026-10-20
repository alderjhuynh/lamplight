package com.aura.lamplight.arcanum.enchantment;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.Optional;
import java.util.Set;

public enum SlotType {
    PROTECTION(
            "lamplight.slot.protection",
            Set.of(
                    Enchantments.PROTECTION,
                    Enchantments.FIRE_PROTECTION,
                    Enchantments.BLAST_PROTECTION,
                    Enchantments.PROJECTILE_PROTECTION,
                    ArcanumEnchantments.DRAGONHEART
            )
    ),
    SECONDARY(
            "lamplight.slot.secondary",
            Set.of(
                    Enchantments.AQUA_AFFINITY,
                    Enchantments.FEATHER_FALLING,
                    Enchantments.SOUL_SPEED,
                    Enchantments.SWIFT_SNEAK,
                    Enchantments.THORNS,
                    ArcanumEnchantments.AERODYNAMICS,
                    ArcanumEnchantments.DASH,
                    ArcanumEnchantments.DOUBLE_JUMP,
                    ArcanumEnchantments.COLD_STEEL,
                    ArcanumEnchantments.DARKNESS_CLOAK,
                    ArcanumEnchantments.FIRE_SHIELD,
                    ArcanumEnchantments.FLAME_WALKER,
                    ArcanumEnchantments.ICE_SHIELD,
                    ArcanumEnchantments.LEAPING,
                    ArcanumEnchantments.NIGHT_VISION,
                    ArcanumEnchantments.REGROWTH,
                    ArcanumEnchantments.SATURATION,
                    ArcanumEnchantments.ANCHOR,
                    ArcanumEnchantments.WATER_BREATHING
            )
    ),
    TOOL(
            "lamplight.slot.tool",
            Set.of(
                    Enchantments.EFFICIENCY,
                    Enchantments.FORTUNE,
                    Enchantments.SILK_TOUCH,
                    Enchantments.LURE
            )
    ),
    MAIN_DAMAGE(
            "lamplight.slot.main_damage",
            Set.of(
                    Enchantments.BREACH,
                    Enchantments.CHANNELING,
                    Enchantments.LOOTING,
                    Enchantments.PIERCING,
                    Enchantments.RIPTIDE
            )
    ),
    SECONDARY_DAMAGE(
            "lamplight.slot.secondary_damage",
            Set.of(
                    Enchantments.BANE_OF_ARTHROPODS,
                    Enchantments.FIRE_ASPECT,
                    Enchantments.SMITE,
                    Enchantments.LUNGE,
                    Enchantments.WIND_BURST,
                    ArcanumEnchantments.ICE_ASPECT
            )
    ),
    MAIN_EFFECT(
            "lamplight.slot.main_effect",
            Set.of(
                    Enchantments.POWER,
                    Enchantments.MULTISHOT,
                    Enchantments.QUICK_CHARGE,
                    ArcanumEnchantments.PROPULSION
            )
    ),
    SECONDARY_EFFECT(
            "lamplight.slot.secondary_effect",
            Set.of(
                    Enchantments.FLAME,
                    Enchantments.INFINITY,
                    ArcanumEnchantments.FROST
            )
    );

    private final String translationKey;
    private final Set<ResourceKey<Enchantment>> enchantKeys;

    SlotType(String translationKey, Set<ResourceKey<Enchantment>> enchantKeys) {
        this.translationKey = translationKey;
        this.enchantKeys = enchantKeys;
    }

    public String translationKey() {
        return translationKey;
    }

    public Component displayName() {
        return Component.translatable(translationKey);
    }

    public boolean contains(Holder<Enchantment> holder) {
        Optional<ResourceKey<Enchantment>> key = holder.unwrapKey();
        return key.isPresent() && enchantKeys.contains(key.get());
    }

    public static Optional<SlotType> forEnchantment(Holder<Enchantment> holder) {
        for (SlotType type : values()) {
            if (type.contains(holder)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

    public static Optional<SlotType> forKey(ResourceKey<Enchantment> key) {
        for (SlotType type : values()) {
            if (type.enchantKeys.contains(key)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }
}
