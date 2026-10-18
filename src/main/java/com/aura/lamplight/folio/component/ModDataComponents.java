package com.aura.lamplight.folio.component;

import com.aura.lamplight.folio.Folio;
import java.util.function.UnaryOperator;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

/**
 * Holds every custom {@link DataComponentType} Folio adds to items.
 *
 * <p>{@link #SPELL} is the component that turns a generic host item (like
 * {@code lamplight:ice_scroll}) into a specific spell. It just stores the
 * {@link Identifier} of the spell in {@link com.aura.lamplight.folio.spell.Spells},
 * so a single registered item can represent every spell of its school
 * without needing one registered item per spell.</p>
 */
public final class ModDataComponents {
    private ModDataComponents() {}

    /**
     * The spell bound to this stack, e.g. {@code lamplight:frost_bolt}.
     * Persistent (saved to disk) and network-synchronized (sent to the
     * client so tooltips/rendering can react to it).
     */
    public static final DataComponentType<Identifier> SPELL = register(
        "spell", builder -> builder.persistent(Identifier.CODEC).networkSynchronized(Identifier.STREAM_CODEC)
    );

    private static <T> DataComponentType<T> register(final String name, final UnaryOperator<DataComponentType.Builder<T>> builder) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Folio.id(name), builder.apply(DataComponentType.builder()).build());
    }

    public static void initialize() {
        Folio.LOGGER.info("Registering data components for {}", Folio.MOD_ID);
    }
}
