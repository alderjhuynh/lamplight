package com.aura.lamplight.folio.attribute;

import com.aura.lamplight.folio.Folio;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public final class FolioAttributes {
    private FolioAttributes() {}

    public static final Attribute MANA = register("mana", new RangedAttribute("attribute.name.lamplight.mana", 0.0D, 0.0D, 1024.0D).setSyncable(true));
    public static final Attribute MAX_MANA = register("max_mana", new RangedAttribute("attribute.name.lamplight.max_mana", 50.0D, 0.0D, 1024.0D).setSyncable(true));

    private static Attribute register(String name, Attribute attr) {
        return Registry.register(BuiltInRegistries.ATTRIBUTE, Folio.id(name), attr);
    }

    public static void initialize() {
        Folio.LOGGER.info("Registered folio attributes");
    }
}
