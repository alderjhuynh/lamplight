package com.aura.lamplight.wayfarer.waystone;

import com.aura.lamplight.wayfarer.Wayfarer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public class ModBlocks {
    private ModBlocks() {
    }

    public static final ResourceKey<Block> WAYSTONE_KEY =
        ResourceKey.create(Registries.BLOCK, Wayfarer.id("waystone"));

    public static final Block WAYSTONE = Registry.register(
        BuiltInRegistries.BLOCK,
        WAYSTONE_KEY,
        new WaystoneBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.STONE)
            .strength(-1.0F, 3600000.0F)
            .pushReaction(PushReaction.BLOCK)
            .sound(SoundType.STONE)
            .lightLevel(state -> state.getValue(WaystoneBlock.LIT) ? 12 : 0)
            .setId(WAYSTONE_KEY)));

    public static void register() {
        Wayfarer.LOGGER.info("Registered wayfarer blocks");
    }
}
