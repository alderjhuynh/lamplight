package com.aura.lamplight.puppeteer.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;

public record AnyOfCondition(List<TriggerCondition> conditions) implements TriggerCondition {

    public static final MapCodec<AnyOfCondition> MAP_CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            Codec.lazyInitialized(() -> TriggerConditionTypes.CODEC).listOf().fieldOf("conditions").forGetter(AnyOfCondition::conditions)
        ).apply(instance, AnyOfCondition::new)
    );

    @Override
    public boolean test(final ServerPlayer player) {
        for (TriggerCondition condition : conditions) {
            if (condition.test(player)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public MapCodec<? extends TriggerCondition> codec() {
        return MAP_CODEC;
    }
}
