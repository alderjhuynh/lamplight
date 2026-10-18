package com.aura.lamplight.puppeteer.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public record HasItemComponentCondition(InteractionHand hand, Identifier component, Optional<String> value) implements TriggerCondition {

    private static final Codec<InteractionHand> HAND_CODEC = Codec.STRING.comapFlatMap(
        name -> switch (name) {
            case "main_hand" -> DataResult.success(InteractionHand.MAIN_HAND);
            case "off_hand" -> DataResult.success(InteractionHand.OFF_HAND);
            default -> DataResult.error(() -> "Unknown hand: " + name + " (expected main_hand or off_hand)");
        },
        hand -> hand == InteractionHand.MAIN_HAND ? "main_hand" : "off_hand"
    );

    public static final MapCodec<HasItemComponentCondition> MAP_CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            HAND_CODEC.optionalFieldOf("hand", InteractionHand.MAIN_HAND).forGetter(HasItemComponentCondition::hand),
            Identifier.CODEC.fieldOf("component").forGetter(HasItemComponentCondition::component),
            Codec.STRING.optionalFieldOf("value").forGetter(HasItemComponentCondition::value)
        ).apply(instance, HasItemComponentCondition::new)
    );

    @Override
    public boolean test(final ServerPlayer player) {
        Optional<DataComponentType<?>> type = BuiltInRegistries.DATA_COMPONENT_TYPE.getOptional(component);
        if (type.isEmpty()) {
            return false;
        }

        ItemStack stack = player.getItemInHand(hand);
        if (!stack.has(type.get())) {
            return false;
        }

        return value.isEmpty() || value.get().equals(String.valueOf(stack.get(type.get())));
    }

    @Override
    public MapCodec<? extends TriggerCondition> codec() {
        return MAP_CODEC;
    }
}
