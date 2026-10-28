package com.aura.lamplight.tinkerer.charm;

import com.aura.lamplight.tinkerer.Tinkerer;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.List;

@SuppressWarnings ("all")
public class ModDataComponents {
	private ModDataComponents() {}

	public static final DataComponentType<CharmComponent> CHARM = register(
		"charm",
		DataComponentType.<CharmComponent>builder()
			.persistent(CharmComponent.CODEC)
			.networkSynchronized(CharmComponent.STREAM_CODEC)
			.build()
	);

	private static <T> DataComponentType<T> register(final String name, final DataComponentType<T> type) {
		Identifier id = Tinkerer.id(name);
		return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, id, type);
	}

	public static void initialize() {
		Tinkerer.LOGGER.info("Registered tinkerer data components");
	}
}
