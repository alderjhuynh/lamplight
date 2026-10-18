package com.aura.lamplight.panacea.registry;

import com.aura.lamplight.panacea.effects.FrostResistance;
import com.aura.lamplight.panacea.effects.Vulnerability;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

import static com.aura.lamplight.panacea.Panacea.MOD_ID;

public class ModEffects {
    public static final Holder<MobEffect> VULNERABILITY = registerEffect(
            "vulnerability_effect",
            new Vulnerability(MobEffectCategory.HARMFUL, 0x8AA49D)
    );

    public static final Holder<MobEffect> FROST_RESISTANCE = registerEffect(
            "frost_resistance_effect",
            new FrostResistance(MobEffectCategory.BENEFICIAL, 0x87CEEB)
    );

    private static Holder<MobEffect> registerEffect(String path, MobEffect effect) {
        return Registry.registerForHolder(
                BuiltInRegistries.MOB_EFFECT,
                Identifier.fromNamespaceAndPath(MOD_ID, path),
                effect
        );
    }

    public static void register() {
    }
}
