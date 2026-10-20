package com.aura.lamplight.hearthwright.block;

import com.aura.lamplight.hearthwright.Hearthwright;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

final class HearthwrightBlocks {
    private HearthwrightBlocks() {
    }

    static ResourceKey<Block> blockKey(String name) {
        return ResourceKey.create(Registries.BLOCK, Hearthwright.id(name));
    }

    /**
     * Copies the base block's hardness, resistance, sound, map colour, and tool
     * requirements without inheriting its loot table or translation key.
     */
    @SuppressWarnings("deprecation")
    static BlockBehaviour.Properties derivativeProperties(Block base) {
        return BlockBehaviour.Properties.ofLegacyCopy(base);
    }

    static Item registerBlockItem(String name, Block block) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Hearthwright.id(name));
        // Match vanilla block items, which use the block.lamplight.* translation key.
        return Registry.register(
                BuiltInRegistries.ITEM,
                key,
                new BlockItem(block, new Item.Properties().useBlockDescriptionPrefix().setId(key)));
    }
}
