package com.aura.lamplight.puppeteer.animation;

import com.aura.lamplight.puppeteer.trigger.TriggerCondition;
import com.aura.lamplight.puppeteer.trigger.TriggerConditionTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.resources.Identifier;

public record AnimationDefinition(
    Identifier id,
    int lengthTicks,
    boolean loop,
    int priority,
    TriggerCondition trigger,
    List<AnimationChannel> channels
) {
    public static final Codec<AnimationDefinition> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(AnimationDefinition::id),
            Codec.INT.fieldOf("length_ticks").forGetter(AnimationDefinition::lengthTicks),
            Codec.BOOL.optionalFieldOf("loop", false).forGetter(AnimationDefinition::loop),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(AnimationDefinition::priority),
            TriggerConditionTypes.CODEC.fieldOf("trigger").forGetter(AnimationDefinition::trigger),
            AnimationChannel.CODEC.listOf().fieldOf("channels").forGetter(AnimationDefinition::channels)
        ).apply(instance, AnimationDefinition::new)
    );
}
