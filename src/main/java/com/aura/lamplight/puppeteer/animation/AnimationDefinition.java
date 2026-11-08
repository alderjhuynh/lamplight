package com.aura.lamplight.puppeteer.animation;

import com.aura.lamplight.puppeteer.trigger.TriggerCondition;
import com.aura.lamplight.puppeteer.trigger.TriggerConditionTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.Identifier;

public record AnimationDefinition(
    Identifier id,
    int lengthTicks,
    boolean loop,
    int priority,
    TriggerCondition trigger,
    List<AnimationChannel> channels,
    boolean additive,
    boolean holdLastFrame,
    Optional<EventKind> eventKind,
    Optional<Ambience> ambience,
    Optional<PlaybackRate> playbackRate,
    boolean overridesBase
) {
    public static final Codec<AnimationDefinition> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(AnimationDefinition::id),
            Codec.INT.fieldOf("length_ticks").forGetter(AnimationDefinition::lengthTicks),
            Codec.BOOL.optionalFieldOf("loop", false).forGetter(AnimationDefinition::loop),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(AnimationDefinition::priority),
            TriggerConditionTypes.CODEC.fieldOf("trigger").forGetter(AnimationDefinition::trigger),
            AnimationChannel.CODEC.listOf().fieldOf("channels").forGetter(AnimationDefinition::channels),
            Codec.BOOL.optionalFieldOf("additive", false).forGetter(AnimationDefinition::additive),
            Codec.BOOL.optionalFieldOf("hold_last_frame", false).forGetter(AnimationDefinition::holdLastFrame),
            EventKind.CODEC.optionalFieldOf("event").forGetter(AnimationDefinition::eventKind),
            Ambience.CODEC.optionalFieldOf("ambience").forGetter(AnimationDefinition::ambience),
            PlaybackRate.CODEC.optionalFieldOf("playback_rate").forGetter(AnimationDefinition::playbackRate),
            Codec.BOOL.optionalFieldOf("overrides_base", false).forGetter(AnimationDefinition::overridesBase)
        ).apply(instance, AnimationDefinition::new)
    );

    public enum EventKind {
        JUMP,
        LAND,
        HURT,
        ATTACK,
        ATTACK_HOLD,
        ENTER_WATER,
        AMBIENT,
        EQUIP,
        USE_ITEM,
        SWAP,
        MANUAL;

        public static final Codec<EventKind> CODEC = Codec.STRING.comapFlatMap(
            name -> {
                for (EventKind kind : values()) {
                    if (kind.name().toLowerCase().equals(name)) {
                        return com.mojang.serialization.DataResult.success(kind);
                    }
                }
                return com.mojang.serialization.DataResult.error(() -> "Unknown event kind: " + name);
            },
            kind -> kind.name().toLowerCase()
        );
    }

    public record Ambience(int minIntervalTicks, int maxIntervalTicks) {
        public static final Codec<Ambience> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                Codec.INT.fieldOf("min_interval_ticks").forGetter(Ambience::minIntervalTicks),
                Codec.INT.fieldOf("max_interval_ticks").forGetter(Ambience::maxIntervalTicks)
            ).apply(instance, Ambience::new)
        );
    }

    public record PlaybackRate(float referenceSpeed, float min, float max) {
        public static final Codec<PlaybackRate> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                Codec.FLOAT.fieldOf("reference_speed").forGetter(PlaybackRate::referenceSpeed),
                Codec.FLOAT.optionalFieldOf("min", 0.4F).forGetter(PlaybackRate::min),
                Codec.FLOAT.optionalFieldOf("max", 1.4F).forGetter(PlaybackRate::max)
            ).apply(instance, PlaybackRate::new)
        );

        public float clamp(final float speed) {
            float rate = speed / Math.max(0.0001F, referenceSpeed);
            return Math.max(min, Math.min(max, rate));
        }
    }
}
