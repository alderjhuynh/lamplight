package com.aura.lamplight.wayfarer;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.component.ItemContainerContents;

public final class ModDataComponents {
	private ModDataComponents() {
	}

	public static final DataComponentType<ItemContainerContents> BACKPACK_CONTENTS = register(
		"backpack_contents",
		DataComponentType.<ItemContainerContents>builder()
			.persistent(ItemContainerContents.CODEC)
			.networkSynchronized(ItemContainerContents.STREAM_CODEC)
			.build()
	);

	private static <T> DataComponentType<T> register(final String name, final DataComponentType<T> type) {
		return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Wayfarer.id(name), type);
	}

	public static void initialize() {
		Wayfarer.LOGGER.info("Registered wayfarer data components");
	}
}
