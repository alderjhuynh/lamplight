package com.aura.menagerie.registry;

import com.aura.menagerie.Menagerie;
import com.aura.menagerie.crow.CrowEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.levelgen.Heightmap;

import net.minecraft.world.entity.SpawnPlacements;

public final class MenagerieEntities {
    public static final EntityType<CrowEntity> CROW = register("crow",
            EntityType.Builder.of(CrowEntity::new, MobCategory.CREATURE).sized(0.45F, 0.45F).clientTrackingRange(10));
    public static final Item CROW_SPAWN_EGG = registerEgg("crow_spawn_egg", CROW, 0x0D111C, 0x1C2030);

    private MenagerieEntities() {
    }

    private static <T extends net.minecraft.world.entity.Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        Identifier id = Menagerie.id(name);
        EntityType<T> type = builder.build(ResourceKey.create(Registries.ENTITY_TYPE, id));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, id, type);
    }

    private static Item registerEgg(String name, EntityType<?> type, int base, int spots) {
        Identifier id = Menagerie.id(name);
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        Item egg = new SpawnEggItem(new Item.Properties().setId(key).spawnEgg(type));
        return Registry.register(BuiltInRegistries.ITEM, key, egg);
    }

    public static void init() {
        FabricDefaultAttributeRegistry.register(CROW, CrowEntity.bakeAttributes().build());
        SpawnPlacements.register(CROW, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, CrowEntity::canCrowSpawn);
    }
}
