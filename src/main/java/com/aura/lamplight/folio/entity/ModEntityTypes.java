package com.aura.lamplight.folio.entity;

import com.aura.lamplight.folio.Folio;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.world.entity.monster.zombie.Zombie;
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

    public static final EntityType<FireballEntity> FIREBALL = register(
        "fireball",
        EntityType.Builder.<FireballEntity>of(FireballEntity::new, MobCategory.MISC)
            .noLootTable()
            .noSave()
            .fireImmune()
            .sized(0.5F, 0.5F)
            .clientTrackingRange(8)
            .updateInterval(1)
    );

    public static final EntityType<HolyBoltEntity> HOLY_BOLT = register(
        "holy_bolt",
        EntityType.Builder.<HolyBoltEntity>of(HolyBoltEntity::new, MobCategory.MISC)
            .noLootTable()
            .sized(0.25F, 0.25F)
            .clientTrackingRange(4)
            .updateInterval(10)
    );

    public static final EntityType<BloodThrallEntity> BLOOD_THRALL = register(
        "blood_thrall",
        EntityType.Builder.<BloodThrallEntity>of(BloodThrallEntity::new, MobCategory.MONSTER)
            .noLootTable()
            .noSave()
            .sized(0.6F, 1.95F)
            .eyeHeight(1.74F)
            .clientTrackingRange(8)
    );

    private static <T extends Entity> EntityType<T> register(final String name, final EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Folio.id(name));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }

    public static void initialize() {
        Folio.LOGGER.info("Registering entity types for {}", Folio.MOD_ID);
        FabricDefaultAttributeRegistry.register(BLOOD_THRALL, Zombie.createAttributes().build());
    }
}
