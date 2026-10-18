package com.aura.lamplight.puppeteer.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;

public record AllOfCondition(List<TriggerCondition> conditions) implements TriggerCondition {

    public static final MapCodec<AllOfCondition> MAP_CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            Codec.lazyInitialized(() -> TriggerConditionTypes.CODEC).listOf().fieldOf("conditions").forGetter(AllOfCondition::conditions)
        ).apply(instance, AllOfCondition::new)
    );

    @Override
    public boolean test(final ServerPlayer player) {
        for (TriggerCondition condition : conditions) {
            if (!condition.test(player)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public MapCodec<? extends TriggerCondition> codec() {
        return MAP_CODEC;
    }
}
