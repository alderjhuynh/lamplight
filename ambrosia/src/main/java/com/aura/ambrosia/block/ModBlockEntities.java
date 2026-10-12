package com.aura.ambrosia.block;

import com.aura.ambrosia.Ambrosia;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {
    private ModBlockEntities() {}

    public static final BlockEntityType<CookingPotBlockEntity> COOKING_POT = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, Ambrosia.id("cooking_pot"),
            FabricBlockEntityTypeBuilder.create(CookingPotBlockEntity::new, ModBlocks.COOKING_POT).build());

    public static final BlockEntityType<WineRackBlockEntity> WINE_RACK = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, Ambrosia.id("wine_rack"),
            FabricBlockEntityTypeBuilder.create(WineRackBlockEntity::new, ModBlocks.WINE_RACK).build());

    public static void register() {
        Ambrosia.LOGGER.info("Registering block entities for {}", Ambrosia.MOD_ID);
    }
}
