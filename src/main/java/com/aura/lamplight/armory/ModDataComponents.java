package com.aura.lamplight.armory;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;

public final class ModDataComponents {
	private ModDataComponents() {}

	public static final DataComponentType<Boolean> TWO_HANDED = register(
		"two_handed",
		DataComponentType.<Boolean>builder()
			.persistent(Codec.BOOL)
			.networkSynchronized(ByteBufCodecs.BOOL)
			.build()
	);

	public static final DataComponentType<Boolean> DUAL_WIELDED = register(
		"dual_wielded",
		DataComponentType.<Boolean>builder()
			.persistent(Codec.BOOL)
			.networkSynchronized(ByteBufCodecs.BOOL)
			.build()
	);

	public static final DataComponentType<Boolean> DUAL_WIELD_GHOST = register(
		"dual_wield_ghost",
		DataComponentType.<Boolean>builder()
			.persistent(Codec.BOOL)
			.networkSynchronized(ByteBufCodecs.BOOL)
			.build()
	);

	private static <T> DataComponentType<T> register(final String name, final DataComponentType<T> type) {
		Identifier id = Armory.id(name);
		return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, id, type);
	}

	public static void initialize() {
		Armory.LOGGER.info("Registered armory data components");
	}
}
