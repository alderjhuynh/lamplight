package com.aura.lamplight.armory;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ArmoryEntityTypes {
	private ArmoryEntityTypes() {
	}

	public static final EntityType<ThrownChakram> THROWN_CHAKRAM = register(
			"thrown_chakram",
			EntityType.Builder.<ThrownChakram>of(ThrownChakram::new, MobCategory.MISC)
					.noLootTable()
					.fireImmune()
					.sized(0.5F, 0.5F)
					.clientTrackingRange(10)
					.updateInterval(1));

	private static <T extends Entity> EntityType<T> register(final String name, final EntityType.Builder<T> builder) {
		ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Armory.id(name));
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
	}

	public static void initialize() {
		Armory.LOGGER.info("Registered armory entity types");
	}
}