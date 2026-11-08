package com.aura.lamplight.puppeteer.trigger;

import com.aura.lamplight.puppeteer.PlayerAnimationTracker;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;

public record AttackParityCondition(Side side, boolean match) implements TriggerCondition {

    private static final Codec<Side> SIDE_CODEC = Codec.STRING.comapFlatMap(
        name -> switch (name) {
            case "left" -> DataResult.success(Side.LEFT);
            case "right" -> DataResult.success(Side.RIGHT);
            default -> DataResult.error(() -> "Unknown attack parity side: " + name + " (expected left or right)");
        },
        side -> side == Side.LEFT ? "left" : "right"
    );

    public static final MapCodec<AttackParityCondition> MAP_CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            SIDE_CODEC.optionalFieldOf("side", Side.RIGHT).forGetter(AttackParityCondition::side),
            Codec.BOOL.optionalFieldOf("match", true).forGetter(AttackParityCondition::match)
        ).apply(instance, AttackParityCondition::new)
    );

    @Override
    public boolean test(final ServerPlayer player) {
        return (PlayerAnimationTracker.attackParity(player) ? Side.LEFT : Side.RIGHT) == side == match;
    }

    @Override
    public MapCodec<? extends TriggerCondition> codec() {
        return MAP_CODEC;
    }

    public enum Side {
        LEFT,
        RIGHT
    }
}
