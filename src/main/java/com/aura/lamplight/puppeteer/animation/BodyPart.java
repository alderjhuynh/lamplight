package com.aura.lamplight.puppeteer.animation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

public enum BodyPart {
    HEAD("head"),
    BODY("body"),
    RIGHT_ARM("right_arm"),
    LEFT_ARM("left_arm"),
    RIGHT_LEG("right_leg"),
    LEFT_LEG("left_leg");

    public static final Codec<BodyPart> CODEC = Codec.STRING.comapFlatMap(
        name -> {
            for (BodyPart part : values()) {
                if (part.id.equals(name)) {
                    return DataResult.success(part);
                }
            }
            return DataResult.error(() -> "Unknown body part: " + name);
        },
        part -> part.id
    );

    private final String id;

    BodyPart(final String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }
}
