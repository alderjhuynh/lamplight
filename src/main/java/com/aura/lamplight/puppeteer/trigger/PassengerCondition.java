package com.aura.lamplight.puppeteer.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public record PassengerCondition(Optional<Identifier> entityId, boolean match) implements TriggerCondition {

    public static final MapCodec<PassengerCondition> MAP_CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            Identifier.CODEC.optionalFieldOf("entity").forGetter(PassengerCondition::entityId),
            Codec.BOOL.optionalFieldOf("match", true).forGetter(PassengerCondition::match)
        ).apply(instance, PassengerCondition::new)
    );

    @Override
    public boolean test(final ServerPlayer player) {
        Entity vehicle = player.getVehicle();
        if (vehicle == null) {
            return !match;
        }
        if (entityId.isEmpty()) {
            return match;
        }
        EntityType<?> wanted = BuiltInRegistries.ENTITY_TYPE.getOptional(entityId.get()).orElse(null);
        return (wanted != null && wanted == vehicle.getType()) == match;
    }

    @Override
    public MapCodec<? extends TriggerCondition> codec() {
        return MAP_CODEC;
    }
}
