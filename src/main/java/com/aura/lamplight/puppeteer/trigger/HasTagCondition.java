package com.aura.lamplight.puppeteer.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;

public record HasTagCondition(String tag) implements TriggerCondition {

    public static final MapCodec<HasTagCondition> MAP_CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            Codec.STRING.fieldOf("tag").forGetter(HasTagCondition::tag)
        ).apply(instance, HasTagCondition::new)
    );

    @Override
    public boolean test(final ServerPlayer player) {
        return player.entityTags().contains(tag);
    }

    @Override
    public MapCodec<? extends TriggerCondition> codec() {
        return MAP_CODEC;
    }
}
