package com.aura.lamplight.menagerie.effect;

import com.aura.lamplight.menagerie.Menagerie;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class MenagerieEffects {
    public static final Holder<MobEffect> ANCHORED = register(
            "anchored",
            new AnchoredEffect(MobEffectCategory.HARMFUL, 0x5C86B8)
                    .addAttributeModifier(
                            Attributes.MOVEMENT_SPEED,
                            Menagerie.id("effect.anchored_speed"),
                            -0.10,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    public static final Holder<MobEffect> KINDLED = register(
            "kindled",
            new KindledEffect(MobEffectCategory.BENEFICIAL, 0xE8A45C)
                    .addAttributeModifier(
                            Attributes.MOVEMENT_EFFICIENCY,
                            Menagerie.id("effect.kindled_efficiency"),
                            1.0,
                            AttributeModifier.Operation.ADD_VALUE)
                    .addAttributeModifier(
                            Attributes.MOVEMENT_SPEED,
                            Menagerie.id("effect.kindled_speed"),
                            0.05,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    private MenagerieEffects() {
    }

    private static Holder<MobEffect> register(String path, MobEffect effect) {
        return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Menagerie.id(path), effect);
    }

    public static void init() {
    }
}
