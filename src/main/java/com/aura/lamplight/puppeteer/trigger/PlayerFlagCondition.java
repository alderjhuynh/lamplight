package com.aura.lamplight.puppeteer.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerPlayer;

public record PlayerFlagCondition(Flag flag, boolean match) implements TriggerCondition {

    public static final MapCodec<PlayerFlagCondition> MAP_CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            Flag.CODEC.fieldOf("flag").forGetter(PlayerFlagCondition::flag),
            Codec.BOOL.optionalFieldOf("match", true).forGetter(PlayerFlagCondition::match)
        ).apply(instance, PlayerFlagCondition::new)
    );

    @Override
    public boolean test(final ServerPlayer player) {
        return flag.test(player) == match;
    }

    @Override
    public MapCodec<? extends TriggerCondition> codec() {
        return MAP_CODEC;
    }

    public enum Flag {
        SPRINTING("sprinting", p -> p.isSprinting()),
        SNEAKING("sneaking", p -> p.isShiftKeyDown() || p.isCrouching()),
        SWIMMING("swimming", p -> p.isSwimming()),
        ON_GROUND("on_ground", p -> p.onGround()),
        IN_WATER("in_water", p -> p.isInWater()),
        SUBMERGED("submerged", p -> p.isUnderWater()),
        FALL_FLYING("fall_flying", p -> p.isFallFlying()),
        CLIMBING("climbing", p -> p.onClimbable()),
        USING_ITEM("using_item", p -> p.isUsingItem()),
        PASSENGER("passenger", p -> p.isPassenger()),
        CRAWLING("crawling", p -> !p.isInWater() && p.isVisuallySwimming());

        public static final Codec<Flag> CODEC = Codec.STRING.comapFlatMap(
            name -> {
                for (Flag flag : values()) {
                    if (flag.id.equals(name)) {
                        return DataResult.success(flag);
                    }
                }
                return DataResult.error(() -> "Unknown player flag: " + name);
            },
            flag -> flag.id
        );

        private final String id;
        private final Predicate<ServerPlayer> test;

        Flag(final String id, final Predicate<ServerPlayer> test) {
            this.id = id;
            this.test = test;
        }

        boolean test(final ServerPlayer player) {
            return test.test(player);
        }
    }
}
