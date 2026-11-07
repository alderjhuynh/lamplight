package com.aura.lamplight.puppeteer.trigger;

import com.aura.lamplight.puppeteer.PlayerAnimationTracker;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;

public record MotionDirectionCondition(Direction direction) implements TriggerCondition {

    public static final MapCodec<MotionDirectionCondition> MAP_CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            Direction.CODEC.fieldOf("direction").forGetter(MotionDirectionCondition::direction)
        ).apply(instance, MotionDirectionCondition::new)
    );

    @Override
    public boolean test(final ServerPlayer player) {
        double dx = PlayerAnimationTracker.lastDeltaX(player);
        double dz = PlayerAnimationTracker.lastDeltaZ(player);
        if (dx * dx + dz * dz < 0.0001D) {
            return false;
        }

        double yawRad = Math.toRadians(player.getYRot());
        double forwardX = -Math.sin(yawRad);
        double forwardZ = Math.cos(yawRad);
        double leftX = forwardZ;
        double leftZ = -forwardX;

        double forwardDot = dx * forwardX + dz * forwardZ;
        double leftDot = dx * leftX + dz * leftZ;

        double angle = Math.toDegrees(Math.atan2(leftDot, forwardDot));

        return switch (direction) {
            case FORWARD -> angle >= -45.0D && angle <= 45.0D;
            case BACKWARD -> angle <= -135.0D || angle >= 135.0D;
            case LEFT -> angle > 45.0D && angle < 135.0D;
            case RIGHT -> angle < -45.0D && angle > -135.0D;
        };
    }

    @Override
    public MapCodec<? extends TriggerCondition> codec() {
        return MAP_CODEC;
    }

    public enum Direction {
        FORWARD("forward"),
        BACKWARD("backward"),
        LEFT("left"),
        RIGHT("right");

        public static final Codec<Direction> CODEC = Codec.STRING.comapFlatMap(
            name -> {
                for (Direction direction : values()) {
                    if (direction.id.equals(name)) {
                        return DataResult.success(direction);
                    }
                }
                return DataResult.error(() -> "Unknown motion direction: " + name);
            },
            direction -> direction.id
        );

        private final String id;

        Direction(final String id) {
            this.id = id;
        }
    }
}
