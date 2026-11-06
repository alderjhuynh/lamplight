package com.aura.lamplight;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.aura.lamplight.puppeteer.animation.AnimationChannel;
import com.aura.lamplight.puppeteer.animation.BodyPart;
import com.aura.lamplight.puppeteer.animation.Easing;
import com.aura.lamplight.puppeteer.animation.Keyframe;
import com.aura.lamplight.puppeteer.animation.Vector3;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Sampling maths of the per-bone track (hold, lerp, loop wrap). */
public final class AnimationChannelSampleTest {

    private static AnimationChannel channel(final Keyframe... keys) {
        return new AnimationChannel(BodyPart.HEAD, List.of(keys), List.of());
    }

    @Test
    void holdsOutsideKeys() {
        AnimationChannel channel = channel(
            new Keyframe(0.0F, new Vector3(0.0F, 0.0F, 0.0F), Vector3.ZERO, Easing.LINEAR, false),
            new Keyframe(10.0F, new Vector3(10.0F, 0.0F, 0.0F), Vector3.ZERO, Easing.LINEAR, false)
        );
        assertEquals(0.0F, channel.sample(0.0F)[0].x(), 1.0E-5F);
        assertEquals(10.0F, channel.sample(10.0F)[0].x(), 1.0E-5F);
        assertEquals(10.0F, channel.sample(25.0F)[0].x(), 1.0E-5F);
    }

    @Test
    void interpolatesBetweenKeys() {
        AnimationChannel channel = channel(
            new Keyframe(0.0F, new Vector3(0.0F, 0.0F, 0.0F), Vector3.ZERO, Easing.LINEAR, false),
            new Keyframe(10.0F, new Vector3(10.0F, 0.0F, 0.0F), Vector3.ZERO, Easing.LINEAR, false)
        );
        assertEquals(5.0F, channel.sample(5.0F)[0].x(), 1.0E-5F);
    }

    @Test
    void loopTailBlendsBackToFirstKey() {
        AnimationChannel channel = channel(
            new Keyframe(0.0F, new Vector3(0.0F, 0.0F, 0.0F), Vector3.ZERO, Easing.LINEAR, false),
            new Keyframe(8.0F, new Vector3(8.0F, 0.0F, 0.0F), Vector3.ZERO, Easing.LINEAR, false)
        );
        Vector3[] tail = channel.sample(9.0F, 10.0F);
        assertEquals(4.0F, tail[0].x(), 1.0E-4F, "tail must blend from the last key toward key 0");
        assertEquals(0.04F, channel.sample(9.998F, 10.0F)[0].x(), 0.05F);
    }

    @Test
    void staticChannelReportsIdentity() {
        AnimationChannel empty = new AnimationChannel(BodyPart.HEAD, List.of(), List.of());
        Vector3[] sample = empty.sample(3.0F, 10.0F);
        assertEquals(0.0F, sample[0].x(), 1.0E-5F);
        assertEquals(1.0F, sample[2].y(), 1.0E-5F, "empty scale track samples identity");
    }

    @Test
    void beyondFinalKeyWithoutLoopHolds() {
        AnimationChannel channel = channel(
            new Keyframe(0.0F, Vector3.ONE, new Vector3(1.0F, 1.0F, 1.0F), Easing.LINEAR, false)
        );
        Vector3[] sample = channel.sample(2.0F);
        assertEquals(1.0F, sample[0].x(), 1.0E-5F);
        assertEquals(1.0F, sample[1].y(), 1.0E-5F, "single-key track holds its key values");
        assertEquals(3, sample.length);
    }
}
