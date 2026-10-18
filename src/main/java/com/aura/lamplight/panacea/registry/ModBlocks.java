package com.aura.lamplight.panacea.registry;

import com.aura.lamplight.panacea.Panacea;
import com.aura.lamplight.panacea.block.PotionCauldronBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class ModBlocks {
    public static final ResourceKey<Block> POTION_CAULDRON_KEY = blockKey("potion_cauldron");
    public static final Block POTION_CAULDRON = register(
            "potion_cauldron",
            new PotionCauldronBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.STONE)
                            .requiresCorrectToolForDrops()
                            .strength(2.0F)
                            .noOcclusion()
                            .setId(POTION_CAULDRON_KEY)
            )
    );

    private static ResourceKey<Block> blockKey(String path) {
        return ResourceKey.create(BuiltInRegistries.BLOCK.key(), Identifier.fromNamespaceAndPath(Panacea.MOD_ID, path));
    }

    private static Block register(String path, Block block) {
        Identifier id = Identifier.fromNamespaceAndPath(Panacea.MOD_ID, path);
        Registry.register(BuiltInRegistries.ITEM, id, new BlockItem(block, new Item.Properties().setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), id))));
        return Registry.register(BuiltInRegistries.BLOCK, id, block);
    }

    public static void register() {
    }
}
