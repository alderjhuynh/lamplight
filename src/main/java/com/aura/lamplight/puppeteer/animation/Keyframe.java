package com.aura.lamplight.puppeteer.animation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * A single sample on a bone track.
 *
 * <p>Times are in <strong>ticks</strong> (20 per second) but may be fractional —
 * the Fluid &amp; Foolish clips are densely baked at ~24fps, which lands on
 * fractional ticks like 0.833.</p>
 *
 * <p>{@code smooth} marks a catmull-rom key as used by Bedrock-style
 * {@code "lerp_mode": "catmullrom"} segments: the segment <em>leaving</em> this
 * keyframe is interpolated as a spline instead of linearly.</p>
 */
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
