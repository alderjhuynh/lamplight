package com.aura.lamplight.puppeteer.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;

public record NotCondition(TriggerCondition condition) implements TriggerCondition {

    public static final MapCodec<NotCondition> MAP_CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            Codec.lazyInitialized(() -> TriggerConditionTypes.CODEC).fieldOf("condition").forGetter(NotCondition::condition)
        ).apply(instance, NotCondition::new)
    );

    @Override
    public boolean test(final ServerPlayer player) {
        return !condition.test(player);
    }

    @Override
    public MapCodec<? extends TriggerCondition> codec() {
        return MAP_CODEC;
    }
}
