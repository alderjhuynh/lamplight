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
