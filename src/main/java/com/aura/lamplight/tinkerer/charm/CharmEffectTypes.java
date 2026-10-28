package com.aura.lamplight.tinkerer.charm;

import com.aura.lamplight.tinkerer.Tinkerer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public final class CharmEffectTypes {
	private CharmEffectTypes() {}

	private static final Map<Identifier, MapCodec<? extends CharmEffect>> BY_ID = new HashMap<>();
	private static final Map<MapCodec<? extends CharmEffect>, Identifier> BY_CODEC = new HashMap<>();

	public static final MapCodec<AttributeCharmEffect> ATTRIBUTE = register("attribute", AttributeCharmEffect.MAP_CODEC);
	public static final MapCodec<DodgeCharmEffect> DODGE = register("dodge", DodgeCharmEffect.MAP_CODEC);
	public static final MapCodec<IgniteCharmEffect> IGNITE_ON_HIT = register("ignite_on_hit", IgniteCharmEffect.MAP_CODEC);
	public static final MapCodec<CritCharmEffect> CRIT = register("crit", CritCharmEffect.MAP_CODEC);
	public static final MapCodec<ResistCharmEffect> RESIST_DAMAGE = register("resist_damage", ResistCharmEffect.MAP_CODEC);
	public static final MapCodec<CleanseCharmEffect> CLEANSE = register("cleanse", CleanseCharmEffect.MAP_CODEC);
	public static final MapCodec<AlloyCharmEffect> ALLOY = register("alloy", AlloyCharmEffect.MAP_CODEC);

	public static final Codec<CharmEffect> CODEC = Identifier.CODEC.dispatch(
		"type",
		effect -> BY_CODEC.get(effect.codec()),
		BY_ID::get
	);

	private static <T extends CharmEffect> MapCodec<T> register(final String name, final MapCodec<T> codec) {
		Identifier id = Tinkerer.id(name);
		BY_ID.put(id, codec);
		BY_CODEC.put(codec, id);
		return codec;
	}

	public static void initialize() {
		Tinkerer.LOGGER.info("Registered {} charm effect types", BY_ID.size());
	}
}
