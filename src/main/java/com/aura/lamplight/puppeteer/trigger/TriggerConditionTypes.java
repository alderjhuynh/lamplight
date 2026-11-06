package com.aura.lamplight.puppeteer.trigger;

import com.aura.lamplight.puppeteer.Puppeteer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.Identifier;

public final class TriggerConditionTypes {
    private TriggerConditionTypes() {}

    private static final Map<Identifier, MapCodec<? extends TriggerCondition>> BY_ID = new HashMap<>();
    private static final Map<MapCodec<? extends TriggerCondition>, Identifier> BY_CODEC = new HashMap<>();

    public static final MapCodec<AllOfCondition> ALL_OF = register("all_of", AllOfCondition.MAP_CODEC);
    public static final MapCodec<AnyOfCondition> ANY_OF = register("any_of", AnyOfCondition.MAP_CODEC);
    public static final MapCodec<NotCondition> NOT = register("not", NotCondition.MAP_CODEC);
    public static final MapCodec<HasMobEffectCondition> HAS_MOB_EFFECT = register("has_mob_effect", HasMobEffectCondition.MAP_CODEC);
    public static final MapCodec<AttributeCompareCondition> ATTRIBUTE_COMPARE = register("attribute_compare", AttributeCompareCondition.MAP_CODEC);
    public static final MapCodec<HasTagCondition> HAS_TAG = register("has_tag", HasTagCondition.MAP_CODEC);
    public static final MapCodec<HasItemComponentCondition> HAS_ITEM_COMPONENT = register("has_item_component", HasItemComponentCondition.MAP_CODEC);
    public static final MapCodec<PlayerFlagCondition> FLAG = register("flag", PlayerFlagCondition.MAP_CODEC);
    public static final MapCodec<SpeedCondition> SPEED = register("speed", SpeedCondition.MAP_CODEC);
    public static final MapCodec<FallDistanceCondition> FALL_DISTANCE = register("fall_distance", FallDistanceCondition.MAP_CODEC);
    public static final MapCodec<ViewPitchCondition> VIEW_PITCH = register("view_pitch", ViewPitchCondition.MAP_CODEC);
    public static final MapCodec<MotionDirectionCondition> MOTION_DIRECTION = register("motion_direction", MotionDirectionCondition.MAP_CODEC);
    public static final MapCodec<HeldItemGroupCondition> HELD_ITEM_GROUP = register("held_item_group", HeldItemGroupCondition.MAP_CODEC);
    public static final MapCodec<PassengerCondition> PASSENGER = register("passenger", PassengerCondition.MAP_CODEC);
    public static final MapCodec<BiomeTemperatureCondition> BIOME_TEMPERATURE = register("biome_temperature", BiomeTemperatureCondition.MAP_CODEC);
    public static final MapCodec<EntityPoseCondition> POSE = register("entity_pose", EntityPoseCondition.MAP_CODEC);
    public static final MapCodec<ManualCondition> MANUAL = register("manual", ManualCondition.MAP_CODEC);

    public static final Codec<TriggerCondition> CODEC = Identifier.CODEC.dispatch(
        "type",
        condition -> BY_CODEC.get(condition.codec()),
        BY_ID::get
    );

    private static <T extends TriggerCondition> MapCodec<T> register(final String name, final MapCodec<T> codec) {
        Identifier id = Puppeteer.id(name);
        BY_ID.put(id, codec);
        BY_CODEC.put(codec, id);
        return codec;
    }

    public static void initialize() {
        Puppeteer.LOGGER.info("Registered {} trigger condition types", BY_ID.size());
    }
}
