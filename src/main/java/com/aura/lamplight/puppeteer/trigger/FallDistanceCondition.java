package com.aura.lamplight.puppeteer.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;

public record FallDistanceCondition(float min, float max) implements TriggerCondition {

    public static final MapCodec<FallDistanceCondition> MAP_CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            Codec.FLOAT.optionalFieldOf("min", 0.0F).forGetter(FallDistanceCondition::min),
            Codec.FLOAT.optionalFieldOf("max", Float.MAX_VALUE).forGetter(FallDistanceCondition::max)
        ).apply(instance, FallDistanceCondition::new)
    );

    @Override
    public boolean test(final ServerPlayer player) {
        float distance = (float) player.fallDistance;
        return distance >= min && distance <= max;
    }

    @Override
    public MapCodec<? extends TriggerCondition> codec() {
        return MAP_CODEC;
    }
}
