package com.aura.lamplight.folio.effect;

import com.aura.lamplight.folio.Folio;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class FolioEffects {
    private FolioEffects() {}

    public static final Holder<MobEffect> CHILL = register("chill",
        new SchoolEffect(MobEffectCategory.HARMFUL, 0x7FE0FF)
            .addAttributeModifier(Attributes.MOVEMENT_SPEED, Folio.id("effect.chill_speed"),
                -EffectTuning.CHILL_SLOW_PER_STACK, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
            .addAttributeModifier(Attributes.FLYING_SPEED, Folio.id("effect.chill_flying_speed"),
                -EffectTuning.CHILL_SLOW_PER_STACK, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    public static final Holder<MobEffect> SHOCK = register("shock",
        new SchoolEffect(MobEffectCategory.HARMFUL, 0x29D3F2));

    public static final Holder<MobEffect> RESILIENCE = register("resilience",
        new SchoolEffect(MobEffectCategory.BENEFICIAL, 0x6E9B4B));

    public static final Holder<MobEffect> VOID = register("void",
        new SchoolEffect(MobEffectCategory.HARMFUL, 0x5B1A66));

    public static final Holder<MobEffect> BURN = register("burn",
        new DamageOverTimeEffect(0xF28C1B, SchoolStatus.BURN, FolioDamageTypes.BURN,
            EffectTuning.BURN_DAMAGE_PER_STACK, EffectTuning.BURN_INTERVAL_TICKS));

    public static final Holder<MobEffect> BLEED = register("bleed",
        new DamageOverTimeEffect(0xC40D12, SchoolStatus.BLEED, FolioDamageTypes.BLEED,
            EffectTuning.BLEED_DAMAGE_PER_STACK, EffectTuning.BLEED_INTERVAL_TICKS));

    public static final Holder<MobEffect> SWIRL = register("swirl",
        new SchoolEffect(MobEffectCategory.BENEFICIAL, 0xDCE8EE)
            .addAttributeModifier(Attributes.AIR_DRAG_MODIFIER, Folio.id("effect.swirl_air_drag"),
                -EffectTuning.SWIRL_AIR_DRAG_REDUCTION_PER_STACK, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    public static final Holder<MobEffect> JUDGEMENT = register("judgement",
        new SchoolEffect(MobEffectCategory.HARMFUL, 0xF2C94C));

    public static final Holder<MobEffect> FROZEN = register("frozen", new FrozenEffect(0xBDF3FF));

    private static Holder<MobEffect> register(final String path, final MobEffect effect) {
        return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Folio.id(path), effect);
    }

    public static void initialize() {
        Folio.LOGGER.info("Registered folio effects");
    }
}
