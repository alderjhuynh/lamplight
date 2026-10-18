package com.aura.lamplight.puppeteer.animation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.function.UnaryOperator;
import net.minecraft.util.Ease;

public enum Easing {
    LINEAR("linear", t -> t),
    EASE_IN("ease_in", Ease::inSine),
    EASE_OUT("ease_out", Ease::outSine),
    EASE_IN_OUT("ease_in_out", Ease::inOutSine),
    EASE_IN_QUAD("ease_in_quad", Ease::inQuad),
    EASE_OUT_QUAD("ease_out_quad", Ease::outQuad),
    EASE_IN_OUT_QUAD("ease_in_out_quad", Ease::inOutQuad),
    EASE_IN_CUBIC("ease_in_cubic", Ease::inCubic),
    EASE_OUT_CUBIC("ease_out_cubic", Ease::outCubic),
    EASE_IN_OUT_CUBIC("ease_in_out_cubic", Ease::inOutCubic),
    EASE_IN_BACK("ease_in_back", Ease::inBack),
    EASE_OUT_BACK("ease_out_back", Ease::outBack),
    EASE_IN_OUT_BACK("ease_in_out_back", Ease::inOutBack),
    EASE_IN_ELASTIC("ease_in_elastic", Ease::inElastic),
    EASE_OUT_ELASTIC("ease_out_elastic", Ease::outElastic),
    EASE_IN_OUT_ELASTIC("ease_in_out_elastic", Ease::inOutElastic),
    EASE_IN_BOUNCE("ease_in_bounce", Ease::inBounce),
    EASE_OUT_BOUNCE("ease_out_bounce", Ease::outBounce),
    EASE_IN_OUT_BOUNCE("ease_in_out_bounce", Ease::inOutBounce);

    public static final Codec<Easing> CODEC = Codec.STRING.comapFlatMap(
        name -> {
            for (Easing easing : values()) {
                if (easing.id.equals(name)) {
                    return DataResult.success(easing);
                }
            }
            return DataResult.error(() -> "Unknown easing: " + name);
        },
        easing -> easing.id
    );

    private final String id;
    private final UnaryOperator<Float> curve;

    Easing(final String id, final java.util.function.Function<Float, Float> curve) {
        this.id = id;
        this.curve = curve::apply;
    }

    /** Applies this curve to a normalized 0..1 progress value. */
    public float apply(final float t) {
        return curve.apply(Math.max(0.0F, Math.min(1.0F, t)));
    }
}
