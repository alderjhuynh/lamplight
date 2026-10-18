package com.aura.lamplight.puppeteer.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

public record HasMobEffectCondition(Holder<MobEffect> effect, Optional<Integer> minAmplifier) implements TriggerCondition {

    public static final MapCodec<HasMobEffectCondition> MAP_CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            MobEffect.CODEC.fieldOf("effect").forGetter(HasMobEffectCondition::effect),
            Codec.INT.optionalFieldOf("min_amplifier").forGetter(HasMobEffectCondition::minAmplifier)
        ).apply(instance, HasMobEffectCondition::new)
    );

    @Override
    public boolean test(final ServerPlayer player) {
        MobEffectInstance instance = player.getEffect(effect);
        if (instance == null) {
            return false;
        }
        return minAmplifier.isEmpty() || instance.getAmplifier() >= minAmplifier.get();
    }

    @Override
    public MapCodec<? extends TriggerCondition> codec() {
        return MAP_CODEC;
    }
}
