package com.aura.lamplight.puppeteer.fpanims;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class FpAnimsTags {
    public static final TagKey<Item> LANTERNS;
    public static final TagKey<Block> CHAINS;
    public static final TagKey<Item> BUCKETS;
    public static final TagKey<Block> GLASS_PANES;
    public static final TagKey<Item> TOOLS;
    public static final TagKey<Item> GEMS_TAG;
    public static final TagKey<Block> REPLACEABLE_BY_MUSHROOMS = BlockTags.REPLACEABLE_BY_MUSHROOMS;
    public static final TagKey<Block> SAPLINGS = TagKey.create(Registries.BLOCK, Identifier.withDefaultNamespace("saplings"));

    static {
        LANTERNS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("forge", "lanterns"));
        CHAINS = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("forge", "chains"));
        BUCKETS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("forge", "buckets"));
        GLASS_PANES = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("forge", "glass_panes"));
        TOOLS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("forge", "tools"));
        GEMS_TAG = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("forge", "gems/diamond"));
    }
}
