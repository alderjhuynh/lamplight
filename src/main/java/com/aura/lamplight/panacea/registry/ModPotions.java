package com.aura.lamplight.panacea.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.Potion;

import static com.aura.lamplight.panacea.Panacea.MOD_ID;

public class ModPotions {
    public static final Holder<Potion> VULNERABILITY = register(
            "vulnerability_potion",
            new Potion(
                    "vulnerability",
                    new MobEffectInstance(MobEffects.SPEED, 400, 3),
                    new MobEffectInstance(ModEffects.VULNERABILITY, 400, 2)
            )
    );

    public static final Holder<Potion> LONG_VULNERABILITY = register(
            "long_vulnerability_potion",
            new Potion(
                    "vulnerability",
                    new MobEffectInstance(MobEffects.SPEED, 800, 3),
                    new MobEffectInstance(ModEffects.VULNERABILITY, 800, 2)
            )
    );

    public static final Holder<Potion> STRONG_VULNERABILITY = register(
            "strong_vulnerability_potion",
            new Potion(
                    "vulnerability",
                    new MobEffectInstance(MobEffects.SPEED, 400, 5),
                    new MobEffectInstance(ModEffects.VULNERABILITY, 400, 3)
            )
    );

    public static final Holder<Potion> FROST_RESISTANCE = register(
            "frost_resistance_potion",
            new Potion("frost_resistance", new MobEffectInstance(ModEffects.FROST_RESISTANCE, 3600))
    );

    public static final Holder<Potion> LONG_FROST_RESISTANCE = register(
            "long_frost_resistance_potion",
            new Potion("frost_resistance", new MobEffectInstance(ModEffects.FROST_RESISTANCE, 9600))
    );

    public static final Holder<Potion> FUSE = register(
            "fuse_potion",
            new Potion("fuse", new MobEffectInstance(ModEffects.FUSE, 900))
    );

    public static final Holder<Potion> LONG_FUSE = register(
            "long_fuse_potion",
            new Potion("fuse", new MobEffectInstance(ModEffects.FUSE, 1800))
    );

    public static final Holder<Potion> STRONG_FUSE = register(
            "strong_fuse_potion",
            new Potion("fuse", new MobEffectInstance(ModEffects.FUSE, 450, 1))
    );

    public static final Holder<Potion> SINKING = register(
            "sinking_potion",
            new Potion("sinking", new MobEffectInstance(ModEffects.SINKING, 1800))
    );

    public static final Holder<Potion> LONG_SINKING = register(
            "long_sinking_potion",
            new Potion("sinking", new MobEffectInstance(ModEffects.SINKING, 4800))
    );

    public static final Holder<Potion> UNDYING = register(
            "undying_potion",
            new Potion("undying", new MobEffectInstance(ModEffects.UNDYING, 900))
    );

    public static final Holder<Potion> LONG_UNDYING = register(
            "long_undying_potion",
            new Potion("undying", new MobEffectInstance(ModEffects.UNDYING, 1800))
    );

    public static final Holder<Potion> BLINDNESS = register(
            "blindness_potion",
            new Potion("blindness", new MobEffectInstance(MobEffects.BLINDNESS, 1800))
    );

    public static final Holder<Potion> LONG_BLINDNESS = register(
            "long_blindness_potion",
            new Potion("blindness", new MobEffectInstance(MobEffects.BLINDNESS, 4800))
    );

    public static final Holder<Potion> DARKNESS = register(
            "darkness_potion",
            new Potion("darkness", new MobEffectInstance(MobEffects.DARKNESS, 1800))
    );

    public static final Holder<Potion> LONG_DARKNESS = register(
            "long_darkness_potion",
            new Potion("darkness", new MobEffectInstance(MobEffects.DARKNESS, 4800))
    );

    public static final Holder<Potion> LEVITATION = register(
            "levitation_potion",
            new Potion("levitation", new MobEffectInstance(MobEffects.LEVITATION, 1800))
    );

    public static final Holder<Potion> GLOWING = register(
            "glowing_potion",
            new Potion("glowing", new MobEffectInstance(MobEffects.GLOWING, 3600))
    );

    public static final Holder<Potion> LONG_GLOWING = register(
            "long_glowing_potion",
            new Potion("glowing", new MobEffectInstance(MobEffects.GLOWING, 9600))
    );

    public static final Holder<Potion> HASTE = register(
            "haste_potion",
            new Potion("haste", new MobEffectInstance(MobEffects.HASTE, 3600))
    );

    public static final Holder<Potion> LONG_HASTE = register(
            "long_haste_potion",
            new Potion("haste", new MobEffectInstance(MobEffects.HASTE, 9600))
    );

    public static final Holder<Potion> MINING_FATIGUE = register(
            "mining_fatigue_potion",
            new Potion("mining_fatigue", new MobEffectInstance(MobEffects.MINING_FATIGUE, 3600))
    );

    public static final Holder<Potion> LONG_MINING_FATIGUE = register(
            "long_mining_fatigue_potion",
            new Potion("mining_fatigue", new MobEffectInstance(MobEffects.MINING_FATIGUE, 9600))
    );

    public static final Holder<Potion> HEALTH_BOOST = register(
            "health_boost_potion",
            new Potion("health_boost", new MobEffectInstance(MobEffects.HEALTH_BOOST, 3600))
    );

    public static final Holder<Potion> LONG_HEALTH_BOOST = register(
            "long_health_boost_potion",
            new Potion("health_boost", new MobEffectInstance(MobEffects.HEALTH_BOOST, 9600))
    );

    public static final Holder<Potion> STRONG_HEALTH_BOOST = register(
            "strong_health_boost_potion",
            new Potion("health_boost", new MobEffectInstance(MobEffects.HEALTH_BOOST, 1800, 1))
    );

    public static final Holder<Potion> UNLUCK = register(
            "unluck_potion",
            new Potion("unluck", new MobEffectInstance(MobEffects.UNLUCK, 6000))
    );

    public static final Holder<Potion> WITHER = register(
            "wither_potion",
            new Potion("wither", new MobEffectInstance(MobEffects.WITHER, 900))
    );

    public static final Holder<Potion> LONG_WITHER = register(
            "long_wither_potion",
            new Potion("wither", new MobEffectInstance(MobEffects.WITHER, 1800))
    );

    public static final Holder<Potion> SHROUDED_INVISIBILITY = register(
            "shrouded_invisibility_potion",
            new Potion("invisibility", new MobEffectInstance(MobEffects.INVISIBILITY, 1800, 1))
    );

    public static final Holder<Potion> TRUE_SIGHT = register(
            "true_sight_potion",
            new Potion("night_vision", new MobEffectInstance(MobEffects.NIGHT_VISION, 1800, 1))
    );

    private static Holder<Potion> register(String path, Potion potion) {
        return Registry.registerForHolder(
                BuiltInRegistries.POTION,
                Identifier.fromNamespaceAndPath(MOD_ID, path),
                potion
        );
    }

    public static void register() {
    }
}
