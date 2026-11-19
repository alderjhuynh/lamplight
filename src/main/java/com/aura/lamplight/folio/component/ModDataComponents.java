package com.aura.lamplight.folio.component;

import com.aura.lamplight.folio.Folio;
import java.util.List;
import java.util.function.UnaryOperator;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public final class ModDataComponents {
    private ModDataComponents() {}

    public static final DataComponentType<Identifier> SPELL = register(
        "spell", builder -> builder.persistent(Identifier.CODEC).networkSynchronized(Identifier.STREAM_CODEC)
    );

    public static final DataComponentType<List<Identifier>> SPELLBOOK_SPELLS = register(
        "spellbook_spells", builder -> builder.persistent(Identifier.CODEC.listOf()).networkSynchronized(Identifier.STREAM_CODEC.apply(net.minecraft.network.codec.ByteBufCodecs.list()))
    );

    public static final DataComponentType<Integer> SPELLBOOK_SELECTED = register(
        "spellbook_selected", builder -> builder.persistent(com.mojang.serialization.Codec.INT).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_INT)
    );

    private static <T> DataComponentType<T> register(final String name, final UnaryOperator<DataComponentType.Builder<T>> builder) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Folio.id(name), builder.apply(DataComponentType.builder()).build());
    }

    public static void initialize() {
        Folio.LOGGER.info("Registering data components for {}", Folio.MOD_ID);
    }
}
