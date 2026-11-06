package com.aura.lamplight.puppeteer.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;

public record BiomeTemperatureCondition(float min, float max) implements TriggerCondition {

    public static final MapCodec<BiomeTemperatureCondition> MAP_CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            Codec.FLOAT.optionalFieldOf("min", -10.0F).forGetter(BiomeTemperatureCondition::min),
            Codec.FLOAT.optionalFieldOf("max", 10.0F).forGetter(BiomeTemperatureCondition::max)
        ).apply(instance, BiomeTemperatureCondition::new)
    );

    @Override
    public boolean test(final ServerPlayer player) {
        if (player.level() == null) {
            return false;
        }
        float temperature = player.level().getBiome(player.blockPosition()).value().getBaseTemperature();
        return temperature >= min && temperature <= max;
    }

    @Override
    public MapCodec<? extends TriggerCondition> codec() {
        return MAP_CODEC;
    }
}
