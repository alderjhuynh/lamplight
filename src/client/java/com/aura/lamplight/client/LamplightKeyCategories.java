package com.aura.lamplight.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

/**
 * Shared key binding categories for the mod.
 *
 * <p>A {@link KeyMapping.Category} may only be registered once, and several
 * client initializers contribute key bindings. They all reference the same
 * instances from here instead of calling {@link KeyMapping.Category#register}
 * themselves, which would throw when a category is registered twice.
 */
public final class LamplightKeyCategories {
    private LamplightKeyCategories() {}

    /** General Lamplight controls. */
    public static final KeyMapping.Category MAIN =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath("lamplight", "lamplight"));
}
