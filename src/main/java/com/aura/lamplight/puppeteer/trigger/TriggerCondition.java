package com.aura.lamplight.puppeteer.trigger;

import com.mojang.serialization.MapCodec;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerPlayer;

public interface TriggerCondition extends Predicate<ServerPlayer> {

    @Override
    boolean test(ServerPlayer player);

    MapCodec<? extends TriggerCondition> codec();
}
