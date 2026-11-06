package com.aura.lamplight.puppeteer.animation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

/**
 * A track for a single (meta) bone: rotation/translation keyframes plus,
 * optionally, a separate scale track (Bedrock clips animate scale in a distinct
 * channel).
 *
 * <p>Scale values are multipliers around 1.0, e.g. {@code [1.2, 0.8, 1.2]}.</p>
 */
public record AnimationChannel(BodyPart part, List<Keyframe> keyframes, List<Keyframe> scaleKeyframes) {

    public AnimationChannel(final BodyPart part, final List<Keyframe> keyframes) {
        this(part, keyframes, List.of());
    }

    public static final Codec<AnimationChannel> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            BodyPart.CODEC.fieldOf("part").forGetter(AnimationChannel::part),
            Keyframe.CODEC.listOf().fieldOf("keyframes").forGetter(AnimationChannel::keyframes),
            Keyframe.CODEC.listOf().optionalFieldOf("scale_keyframes", List.of()).forGetter(AnimationChannel::scaleKeyframes)
        ).apply(instance, AnimationChannel::new)
    );

    /**
     * Samples the track at {@code tick}, returning {@code [rotation, translation, scale]}.
     * Beyond the last keyframe the pose is held (auto-holds one-shots and poses).
     */
    public Vector3[] sample(final float tick) {
        Vector3[] pair = samplePair(keyframes, tick, 0.0F);
        return new Vector3[] {
            pair[0],
            pair[1],
            sampleVector(scaleKeyframes, tick, 0.0F)
        };
    }

    /**
     * Looping variant: when {@code loopLength} is positive, the short tail after the
     * final keyframe blends into the first keyframe so a wrapped loop doesn't pop.
     */
    public Vector3[] sample(final float tick, final float loopLength) {
        Vector3[] pair = samplePair(keyframes, tick, loopLength);
        return new Vector3[] {
            pair[0],
            pair[1],
            sampleVector(scaleKeyframes, tick, loopLength)
        };
    }

    /** Catmull-rom-aware interpolation of the combined rotation/translation keyframes. */
    private static Vector3[] samplePair(final List<Keyframe> keys, final float tick, final float loopLength) {
        if (keys.isEmpty()) {
            return new Vector3[] {Vector3.ZERO, Vector3.ZERO};
        }

        Keyframe first = keys.get(0);
        if (tick <= first.time()) {
            if (tick < first.time() && loopLength > 0.0F) {
                // the wrapped tail before key 0 isn't reachable (tick stays in [0, loopLength))
                // but keep the blend correct if an anchor drifts just past the end
                float wrap = lastKey(keys).time();
                if (wrap < loopLength && tick > wrap) {
                    return wrapBlend(keys, tick, loopLength);
                }
            }
            return new Vector3[] {first.rotation(), first.translation()};
        }

        Keyframe last = keys.get(keys.size() - 1);
        if (tick >= last.time()) {
            if (loopLength > 0.0F && tick < loopLength) {
                return wrapBlend(keys, tick, loopLength);
            }
            return new Vector3[] {last.rotation(), last.translation()};
        }

        for (int i = 0; i < keys.size() - 1; i++) {
            Keyframe from = keys.get(i);
            Keyframe to = keys.get(i + 1);
            if (tick >= from.time() && tick <= to.time()) {
                float t = segmentProgress(from, to, tick);

                if (to.smooth()) {
                    return new Vector3[] {
                        catmullrom(from.rotation(), to.rotation(), neighborBefore(keys, i), neighborAfter(keys, i + 1), t),
                        catmullrom(from.translation(), to.translation(), neighborBefore(keys, i), neighborAfter(keys, i + 1), t)
                    };
                }

                return new Vector3[] {
                    from.rotation().lerp(to.rotation(), t),
                    from.translation().lerp(to.translation(), t)
                };
            }
        }

        return new Vector3[] {last.rotation(), last.translation()};
    }

    /** Linear segment between the final keyframe and keyframe 0 across the loop wrap. */
    private static Vector3[] wrapBlend(final List<Keyframe> keys, final float tick, final float loopLength) {
        Keyframe last = keys.get(keys.size() - 1);
        float span = loopLength - last.time();
        float t = span <= 0.0F ? 1.0F : (tick - last.time()) / span;
        return new Vector3[] {
            last.rotation().lerp(keys.get(0).rotation(), t),
            last.translation().lerp(keys.get(0).translation(), t)
        };
    }

    private static Keyframe lastKey(final List<Keyframe> keys) {
        return keys.get(keys.size() - 1);
    }

    /** Catmull-rom-aware interpolation of the scalar-track keyframes (values in {@link Keyframe#rotation()}). */
    private static Vector3 sampleVector(final List<Keyframe> keys, final float tick, final float loopLength) {
        if (keys.isEmpty()) {
            return Vector3.ONE;
        }

        Keyframe first = keys.get(0);
        if (tick <= first.time()) {
            return first.rotation();
        }

        Keyframe last = keys.get(keys.size() - 1);
        if (tick >= last.time()) {
            if (loopLength > 0.0F && tick < loopLength) {
                float span = loopLength - last.time();
                float t = span <= 0.0F ? 1.0F : (tick - last.time()) / span;
                return last.rotation().lerp(keys.get(0).rotation(), t);
            }
            return last.rotation();
        }

        for (int i = 0; i < keys.size() - 1; i++) {
            Keyframe from = keys.get(i);
            Keyframe to = keys.get(i + 1);
            if (tick >= from.time() && tick <= to.time()) {
                float t = segmentProgress(from, to, tick);

                if (to.smooth()) {
                    return catmullrom(from.rotation(), to.rotation(), neighborBefore(keys, i), neighborAfter(keys, i + 1), t);
                }

                return from.rotation().lerp(to.rotation(), t);
            }
        }

        return last.rotation();
    }

    private static float segmentProgress(final Keyframe from, final Keyframe to, final float tick) {
        float span = to.time() - from.time();
        float t = span <= 0.0F ? 1.0F : (tick - from.time()) / span;
        return to.easing().apply(t);
    }

    /** Spline control point before {@link Keyframe} {@code index}, linearly extrapolated if it does not exist. */
    private static Vector3 neighborBefore(final List<Keyframe> keys, final int index) {
        if (index > 0) {
            return keys.get(index - 1).rotation();
        }
        Keyframe k0 = keys.get(0);
        Keyframe k1 = keys.get(1);
        return k0.rotation().scale(2.0F).minus(k1.rotation());
    }

    /** Spline control point after {@link Keyframe} {@code index}, linearly extrapolated if it does not exist. */
    private static Vector3 neighborAfter(final List<Keyframe> keys, final int index) {
        if (index + 1 < keys.size()) {
            return keys.get(index + 1).rotation();
        }
        Keyframe kn = keys.get(index);
        Keyframe kn1 = keys.get(index - 1);
        return kn.rotation().scale(2.0F).minus(kn1.rotation());
    }

    /** Uniform (tension 0.5) catmull-rom spline segment between {@code p1} and {@code p2}. */
    private static Vector3 catmullrom(final Vector3 p0, final Vector3 p1, final Vector3 p2, final Vector3 p3, final float t) {
        float t2 = t * t;
        float t3 = t2 * t;
        return new Vector3(
            0.5F * (2.0F * p1.x() + (-p0.x() + p2.x()) * t + (2.0F * p0.x() - 5.0F * p1.x() + 4.0F * p2.x() - p3.x()) * t2 + (-p0.x() + 3.0F * p1.x() - 3.0F * p2.x() + p3.x()) * t3),
            0.5F * (2.0F * p1.y() + (-p0.y() + p2.y()) * t + (2.0F * p0.y() - 5.0F * p1.y() + 4.0F * p2.y() - p3.y()) * t2 + (-p0.y() + 3.0F * p1.y() - 3.0F * p2.y() + p3.y()) * t3),
            0.5F * (2.0F * p1.z() + (-p0.z() + p2.z()) * t + (2.0F * p0.z() - 5.0F * p1.z() + 4.0F * p2.z() - p3.z()) * t2 + (-p0.z() + 3.0F * p1.z() - 3.0F * p2.z() + p3.z()) * t3)
        );
    }
}
