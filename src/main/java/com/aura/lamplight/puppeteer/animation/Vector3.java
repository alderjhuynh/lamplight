package com.aura.lamplight.puppeteer.animation;

import com.mojang.serialization.Codec;
import java.util.List;

public record Vector3(float x, float y, float z) {

    public static final Vector3 ZERO = new Vector3(0.0F, 0.0F, 0.0F);

    public static final Codec<Vector3> CODEC = Codec.FLOAT.listOf().comapFlatMap(
        list -> {
            if (list.size() != 3) {
                return com.mojang.serialization.DataResult.error(() -> "Expected an array of exactly 3 numbers, got " + list.size());
            }
            return com.mojang.serialization.DataResult.success(new Vector3(list.get(0), list.get(1), list.get(2)));
        },
        vec -> List.of(vec.x, vec.y, vec.z)
    );

    public Vector3 plus(final Vector3 other) {
        return new Vector3(x + other.x, y + other.y, z + other.z);
    }

    public Vector3 lerp(final Vector3 target, final float t) {
        return new Vector3(
            x + (target.x - x) * t,
            y + (target.y - y) * t,
            z + (target.z - z) * t
        );
    }
}
