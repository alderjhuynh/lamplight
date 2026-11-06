package com.aura.lamplight.puppeteer.trigger;

import com.mojang.serialization.MapCodec;
import net.minecraft.server.level.ServerPlayer;

public record ManualCondition() implements TriggerCondition {

    public static final MapCodec<ManualCondition> MAP_CODEC = MapCodec.unit(ManualCondition::new);

    @Override
    public boolean test(final ServerPlayer player) {
        return false;
    }

    @Override
    public MapCodec<? extends TriggerCondition> codec() {
        return MAP_CODEC;
    }
}
