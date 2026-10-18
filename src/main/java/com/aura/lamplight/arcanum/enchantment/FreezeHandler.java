package com.aura.lamplight.arcanum.enchantment;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public final class FreezeHandler {
    private FreezeHandler() {}

   public static void applyFreeze(LivingEntity target, int level) {
        if (target == null || target.level().isClientSide()) return;
        if (level <= 0) return;
        if (target.isDeadOrDying()) return;

        int base = 200;
        int extraPerLevel = 60;
        int freezeTicks = base + Math.max(0, level - 1) * extraPerLevel;
        target.setTicksFrozen(Math.max(target.getTicksFrozen(), freezeTicks));

        int duration = 60 + level * 40; // 100 @1, 140 @2, 180 @3 ...
        int amplifier = Math.max(0, level - 1); // 0 = Slowness I
        if (amplifier > 1) amplifier = 1;

        MobEffectInstance slowness = new MobEffectInstance(
                MobEffects.SLOWNESS,
                duration,
                amplifier,
                false, true, true
        );
        target.addEffect(slowness);
    }

    public static int getLevel(ItemStack stack, ResourceKey<Enchantment> key) {
        if (stack == null || stack.isEmpty()) return 0;
        ItemEnchantments enchants = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (Holder<Enchantment> holder : enchants.keySet()) {
            if (holder.unwrapKey().map(k -> k.equals(key)).orElse(false)) {
                return enchants.getLevel(holder);
            }
        }
        return 0;
    }

    public static int getFrostLevel(ItemStack stack) {
        return getLevel(stack, ArcanumEnchantments.FROST);
    }

    public static int getIceAspectLevel(ItemStack stack) {
        return getLevel(stack, ArcanumEnchantments.ICE_ASPECT);
    }
}
