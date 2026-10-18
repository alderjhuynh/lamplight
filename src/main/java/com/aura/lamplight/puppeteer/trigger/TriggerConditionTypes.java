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
