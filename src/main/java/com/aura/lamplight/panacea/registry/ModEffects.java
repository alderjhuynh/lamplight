package com.aura.lamplight.panacea.registry;

import com.aura.lamplight.panacea.effects.FrostResistance;
import com.aura.lamplight.panacea.effects.Fuse;
import com.aura.lamplight.panacea.effects.Sinking;
import com.aura.lamplight.panacea.effects.Undying;
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

    public static final Holder<MobEffect> FUSE = registerEffect(
            "fuse_effect",
            new Fuse(MobEffectCategory.HARMFUL, 0xC0392B)
    );

    public static final Holder<MobEffect> SINKING = registerEffect(
            "sinking_effect",
            new Sinking(MobEffectCategory.HARMFUL, 0x3A7A8C)
    );

    public static final Holder<MobEffect> UNDYING = registerEffect(
            "undying_effect",
            new Undying(MobEffectCategory.BENEFICIAL, 0x36C55F)
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
