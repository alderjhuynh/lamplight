package com.aura.lamplight.puppeteer.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;

public record EntityPoseCondition(Pose pose, boolean match) implements TriggerCondition {

    public static final MapCodec<EntityPoseCondition> MAP_CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            Pose.CODEC.fieldOf("pose").forGetter(EntityPoseCondition::pose),
            Codec.BOOL.optionalFieldOf("match", true).forGetter(EntityPoseCondition::match)
        ).apply(instance, EntityPoseCondition::new)
    );

    @Override
    public boolean test(final ServerPlayer player) {
        return (player.getPose() == pose) == match;
    }

    @Override
    public MapCodec<? extends TriggerCondition> codec() {
        return MAP_CODEC;
    }
}
