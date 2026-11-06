package com.aura.lamplight.puppeteer.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;

public record ViewPitchCondition(float min, float max) implements TriggerCondition {

    public static final MapCodec<ViewPitchCondition> MAP_CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            Codec.FLOAT.optionalFieldOf("min", -180.0F).forGetter(ViewPitchCondition::min),
            Codec.FLOAT.optionalFieldOf("max", 180.0F).forGetter(ViewPitchCondition::max)
        ).apply(instance, ViewPitchCondition::new)
    );

    @Override
    public boolean test(final ServerPlayer player) {
        return player.getXRot() >= min && player.getXRot() <= max;
    }

    @Override
    public MapCodec<? extends TriggerCondition> codec() {
        return MAP_CODEC;
    }
}
