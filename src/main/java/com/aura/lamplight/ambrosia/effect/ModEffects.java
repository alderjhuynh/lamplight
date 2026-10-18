package com.aura.lamplight.ambrosia.effect;

import com.aura.lamplight.ambrosia.Ambrosia;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;

public class ModEffects {
    public static final Holder<MobEffect> NOURISHMENT = register(
            "nourishment",
            new NourishmentEffect(MobEffectCategory.BENEFICIAL, 0xE8B64A)
    );

    public static final Holder<MobEffect> FAMISHED = register(
            "famished",
            new FamishedEffect(MobEffectCategory.BENEFICIAL, 0xCC5A2A)
    );

    public static final Holder<MobEffect> COMFORT = register(
        "comfort",
        new ComfortEffect(MobEffectCategory.BENEFICIAL, 0xE8A87C)
    );

    private static Holder<MobEffect> register(String path, MobEffect effect) {
        return Registry.registerForHolder(
                BuiltInRegistries.MOB_EFFECT,
                Ambrosia.id(path),
                effect
        );
    }

    public static MobEffectInstance nourishment(int durationTicks) {
        return new MobEffectInstance(NOURISHMENT, durationTicks, 0, false, false, true);
    }

    public static MobEffectInstance famished(int durationTicks) {
        return new MobEffectInstance(FAMISHED, durationTicks, 0, false, false, true);
    }

    public static MobEffectInstance comfort(int durationTicks) {
        return new MobEffectInstance(COMFORT, durationTicks, 0, false, false, true);
    }

    public static void register() {
    }
}
