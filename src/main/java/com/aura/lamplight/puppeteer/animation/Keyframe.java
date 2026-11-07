package com.aura.lamplight.puppeteer.animation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record Keyframe(float time, Vector3 rotation, Vector3 translation, Easing easing, boolean smooth) {

    public static final Codec<Keyframe> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            Codec.FLOAT.fieldOf("time").forGetter(Keyframe::time),
            Vector3.CODEC.optionalFieldOf("rotation", Vector3.ZERO).forGetter(Keyframe::rotation),
            Vector3.CODEC.optionalFieldOf("translation", Vector3.ZERO).forGetter(Keyframe::translation),
            Easing.CODEC.optionalFieldOf("easing", Easing.LINEAR).forGetter(Keyframe::easing),
            Codec.BOOL.optionalFieldOf("smooth", false).forGetter(Keyframe::smooth)
        ).apply(instance, Keyframe::new)
    );

    public static Keyframe rotation(final float time, final Vector3 rotation, final boolean smooth) {
        return new Keyframe(time, rotation, Vector3.ZERO, Easing.LINEAR, smooth);
    }
}
