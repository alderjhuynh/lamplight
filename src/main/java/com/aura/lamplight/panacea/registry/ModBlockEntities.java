package com.aura.lamplight.panacea.registry;

import com.aura.lamplight.panacea.Panacea;
import com.aura.lamplight.panacea.block.PotionCauldronBlock;
import com.aura.lamplight.panacea.blockentity.PotionCauldronBlockEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Set;

public class ModBlockEntities {
    public static final BlockEntityType<PotionCauldronBlockEntity> POTION_CAULDRON = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Panacea.id("potion_cauldron"),
            new BlockEntityType<>(
                    PotionCauldronBlockEntity::new,
                    Set.of(ModBlocks.POTION_CAULDRON)
            )
    );

    public static void register() {
    }
}
