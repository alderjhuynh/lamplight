package com.aura.lamplight.puppeteer.trigger;

import com.aura.lamplight.puppeteer.PlayerAnimationTracker;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;

public record SpeedCondition(float min, float max) implements TriggerCondition {

    public static final MapCodec<SpeedCondition> MAP_CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            Codec.FLOAT.optionalFieldOf("min", 0.0F).forGetter(SpeedCondition::min),
            Codec.FLOAT.optionalFieldOf("max", Float.MAX_VALUE).forGetter(SpeedCondition::max)
        ).apply(instance, SpeedCondition::new)
    );

    @Override
    public boolean test(final ServerPlayer player) {
        float speed = PlayerAnimationTracker.measuredSpeed(player);
        return speed >= min && speed <= max;
    }

    @Override
    public MapCodec<? extends TriggerCondition> codec() {
        return MAP_CODEC;
    }
}
