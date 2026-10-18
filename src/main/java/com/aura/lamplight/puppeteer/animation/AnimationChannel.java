package com.aura.lamplight.puppeteer.animation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

public record AnimationChannel(BodyPart part, List<Keyframe> keyframes) {

    public static final Codec<AnimationChannel> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            BodyPart.CODEC.fieldOf("part").forGetter(AnimationChannel::part),
            Keyframe.CODEC.listOf().fieldOf("keyframes").forGetter(AnimationChannel::keyframes)
        ).apply(instance, AnimationChannel::new)
    );

    public Vector3[] sample(final float tick) {
        if (keyframes.isEmpty()) {
            return new Vector3[] {Vector3.ZERO, Vector3.ZERO};
        }

        Keyframe first = keyframes.get(0);
        if (tick <= first.time()) {
            return new Vector3[] {first.rotation(), first.translation()};
        }

        Keyframe last = keyframes.get(keyframes.size() - 1);
        if (tick >= last.time()) {
            return new Vector3[] {last.rotation(), last.translation()};
        }

        for (int i = 0; i < keyframes.size() - 1; i++) {
            Keyframe from = keyframes.get(i);
            Keyframe to = keyframes.get(i + 1);
            if (tick >= from.time() && tick <= to.time()) {
                float span = to.time() - from.time();
                float t = span <= 0.0F ? 1.0F : (tick - from.time()) / span;
                float eased = to.easing().apply(t);
                return new Vector3[] {
                    from.rotation().lerp(to.rotation(), eased),
                    from.translation().lerp(to.translation(), eased)
                };
            }
        }

        return new Vector3[] {last.rotation(), last.translation()};
    }
}
