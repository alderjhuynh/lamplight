package com.aura.lamplight.menagerie.registry;

import com.aura.lamplight.menagerie.Menagerie;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class MenagerieTags {
    public static final TagKey<EntityType<?>> SCATTERS_CROWS = entityTag("scatters_crows");
    public static final TagKey<Block> CROW_FORAGEABLES = blockTag("crow_forageables");
    public static final TagKey<Item> CROW_FOODSTUFFS = itemTag("crow_foodstuffs");
    public static final TagKey<Item> CROW_TAMEABLES = itemTag("crow_tameables");
    public static final TagKey<Item> CROW_BREEDABLES = itemTag("crow_breedables");
    public static final TagKey<Item> CROW_FORAGED_SEEDS = itemTag("crow_foraged_seeds");

    public static final TagKey<EntityType<?>> SCATTERS_WISPS = entityTag("scatters_wisps");
    public static final TagKey<Item> WISP_TAMEABLES = itemTag("wisp_tameables");
    public static final TagKey<Item> WISP_FOODSTUFFS = itemTag("wisp_foodstuffs");
    public static final TagKey<Item> WISP_BREEDABLES = itemTag("wisp_breedables");
    public static final TagKey<Block> WISP_POLLINATES = blockTag("wisp_pollinates");
    public static final TagKey<Block> WISP_LURES = blockTag("wisp_lures");
    public static final TagKey<Block> WISP_RECHARGE_BLOCKS = blockTag("wisp_recharge_blocks");

    public static final TagKey<Item> RACCOON_FOODSTUFFS = itemTag("raccoon_foodstuffs");
    public static final TagKey<Item> RACCOON_BREEDABLES = itemTag("raccoon_breedables");
    public static final TagKey<Block> RACCOON_DIGGABLE = blockTag("raccoon_diggable");

    private MenagerieTags() {
    }

    private static TagKey<EntityType<?>> entityTag(String name) {
        return TagKey.create(Registries.ENTITY_TYPE, Menagerie.id(name));
    }

    private static TagKey<Item> itemTag(String name) {
        return TagKey.create(Registries.ITEM, Menagerie.id(name));
    }

    private static TagKey<Block> blockTag(String name) {
        return TagKey.create(Registries.BLOCK, Menagerie.id(name));
    }
}
