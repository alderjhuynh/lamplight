package com.aura.lamplight.puppeteer.animation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record Keyframe(int time, Vector3 rotation, Vector3 translation, Easing easing) {

    public static final Codec<Keyframe> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            Codec.INT.fieldOf("time").forGetter(Keyframe::time),
            Vector3.CODEC.optionalFieldOf("rotation", Vector3.ZERO).forGetter(Keyframe::rotation),
            Vector3.CODEC.optionalFieldOf("translation", Vector3.ZERO).forGetter(Keyframe::translation),
            Easing.CODEC.optionalFieldOf("easing", Easing.LINEAR).forGetter(Keyframe::easing)
        ).apply(instance, Keyframe::new)
    );
}
