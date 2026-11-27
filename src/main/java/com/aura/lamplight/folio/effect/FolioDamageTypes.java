package com.aura.lamplight.folio.effect;

import com.aura.lamplight.folio.Folio;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

public final class FolioDamageTypes {
    private FolioDamageTypes() {}

    public static final ResourceKey<DamageType> BURN = key("folio_burn");
    public static final ResourceKey<DamageType> BURN_EXPLOSION = key("folio_burn_explosion");
    public static final ResourceKey<DamageType> BLEED = key("folio_bleed");
    public static final ResourceKey<DamageType> RUPTURE = key("folio_rupture");
    public static final ResourceKey<DamageType> RETRIBUTION = key("folio_retribution");
    public static final ResourceKey<DamageType> SHOCK_CHAIN = key("folio_shock_chain");

    private static ResourceKey<DamageType> key(final String path) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, Folio.id(path));
    }
}
