package com.aura.lamplight.puppeteer.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;

public record AttributeCompareCondition(Holder<Attribute> attribute, Operator operator, double value) implements TriggerCondition {

    public static final MapCodec<AttributeCompareCondition> MAP_CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            Attribute.CODEC.fieldOf("attribute").forGetter(AttributeCompareCondition::attribute),
            Operator.CODEC.fieldOf("operator").forGetter(AttributeCompareCondition::operator),
            Codec.DOUBLE.fieldOf("value").forGetter(AttributeCompareCondition::value)
        ).apply(instance, AttributeCompareCondition::new)
    );

    @Override
    public boolean test(final ServerPlayer player) {
        if (player.getAttribute(attribute) == null) {
            return false;
        }
        double current = player.getAttributeValue(attribute);
        return operator.test(current, value);
    }

    @Override
    public MapCodec<? extends TriggerCondition> codec() {
        return MAP_CODEC;
    }

    public enum Operator {
        GREATER_THAN((a, b) -> a > b),
        GREATER_OR_EQUAL((a, b) -> a >= b),
        LESS_THAN((a, b) -> a < b),
        LESS_OR_EQUAL((a, b) -> a <= b),
        EQUAL((a, b) -> a == b);

        public static final Codec<Operator> CODEC = Codec.STRING.comapFlatMap(
            name -> {
                for (Operator operator : values()) {
                    if (operator.name().equalsIgnoreCase(name)) {
                        return DataResult.success(operator);
                    }
                }
                return DataResult.error(() -> "Unknown comparison operator: " + name);
            },
            operator -> operator.name().toLowerCase()
        );

        private final DoubleCompare compare;

        Operator(final DoubleCompare compare) {
            this.compare = compare;
        }

        public boolean test(final double a, final double b) {
            return compare.test(a, b);
        }

        @FunctionalInterface
        private interface DoubleCompare {
            boolean test(double a, double b);
        }
    }
}
