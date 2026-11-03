package com.aura.lamplight.wayfarer.waystone;

import com.aura.lamplight.wayfarer.Wayfarer;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {
    private ModBlockEntities() {}

    public static final BlockEntityType<WaystoneBlockEntity> WAYSTONE = Registry.register(
        BuiltInRegistries.BLOCK_ENTITY_TYPE, Wayfarer.id("waystone"),
        FabricBlockEntityTypeBuilder.create(WaystoneBlockEntity::new, ModBlocks.WAYSTONE).build());

    public static void register() {
        Wayfarer.LOGGER.info("Registered wayfarer block entities");
    }
}
