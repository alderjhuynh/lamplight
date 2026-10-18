package com.aura.lamplight.folio.entity;

import com.aura.lamplight.folio.Folio;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntityTypes {
    private ModEntityTypes() {}

    public static final EntityType<FrostBoltEntity> FROST_BOLT = register(
        "frost_bolt",
        EntityType.Builder.<FrostBoltEntity>of(FrostBoltEntity::new, MobCategory.MISC)
            .noLootTable()
            .sized(0.25F, 0.25F)
            .clientTrackingRange(4)
            .updateInterval(10)
    );

    private static <T extends Entity> EntityType<T> register(final String name, final EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Folio.id(name));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }

    public static void initialize() {
        Folio.LOGGER.info("Registering entity types for {}", Folio.MOD_ID);
    }
}
