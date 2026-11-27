package com.aura.lamplight.folio.effect;

import com.aura.lamplight.folio.Folio;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;

public final class FolioEffectTags {
    private FolioEffectTags() {}

    public static final TagKey<EntityType<?>> FREEZE_IMMUNE =
        TagKey.create(Registries.ENTITY_TYPE, Folio.id("freeze_immune"));

    public static final TagKey<DamageType> MELEE = TagKey.create(Registries.DAMAGE_TYPE, Folio.id("is_melee"));

    public static final TagKey<DamageType> RANGED = TagKey.create(Registries.DAMAGE_TYPE, Folio.id("is_ranged"));

    public static final TagKey<DamageType> NO_SHOCK_CHAIN =
        TagKey.create(Registries.DAMAGE_TYPE, Folio.id("no_shock_chain"));
}
