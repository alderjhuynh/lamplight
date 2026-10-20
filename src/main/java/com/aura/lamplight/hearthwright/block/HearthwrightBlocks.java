package com.aura.lamplight.hearthwright.block;

import com.aura.lamplight.hearthwright.Hearthwright;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

final class HearthwrightBlocks {
    private HearthwrightBlocks() {
    }

    static ResourceKey<Block> blockKey(String name) {
        return ResourceKey.create(Registries.BLOCK, Hearthwright.id(name));
    }

    static Item registerBlockItem(String name, Block block) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Hearthwright.id(name));
        return Registry.register(
                BuiltInRegistries.ITEM,
                key,
                new BlockItem(block, new Item.Properties().setId(key)));
    }
}
