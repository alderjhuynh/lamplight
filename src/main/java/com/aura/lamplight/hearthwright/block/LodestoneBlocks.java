package com.aura.lamplight.hearthwright.block;

import com.aura.lamplight.hearthwright.Hearthwright;
import com.aura.lamplight.mixin.hearthwright.PoiTypesAccessor;
import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public final class LodestoneBlocks {
    private static final List<Block> LODESTONE_BLOCKS = new ArrayList<>();

    private LodestoneBlocks() {
    }

    public static void register() {
        ResourceKey<Block> key = HearthwrightBlocks.blockKey("sandstone_lodestone");
        Block block = Registry.register(
                BuiltInRegistries.BLOCK,
                key,
                new Block(HearthwrightBlocks.derivativeProperties(Blocks.LODESTONE).setId(key)) {
                    @Override
                    public MapCodec<? extends Block> codec() {
                        return BlockBehaviour.simpleCodec(properties -> this);
                    }
                });
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Hearthwright.id("sandstone_lodestone"));
        Registry.register(
                BuiltInRegistries.ITEM,
                itemKey,
                new BlockItem(block, new Item.Properties().useBlockDescriptionPrefix().setId(itemKey)));
        registerLodestoneStates(block);
        Hearthwright.LOGGER.info("Registered sandstone lodestone block");
    }

    public static void addCreativeTabEntries() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                .register(output -> LODESTONE_BLOCKS.forEach(output::accept));
    }

    public static boolean isLodestone(BlockState state) {
        return LODESTONE_BLOCKS.stream().anyMatch(block -> state.is(block));
    }

    private static void registerLodestoneStates(Block block) {
        Holder<PoiType> lodestone = BuiltInRegistries.POINT_OF_INTEREST_TYPE.getOrThrow(PoiTypes.LODESTONE);
        Map<BlockState, Holder<PoiType>> statesByType = PoiTypesAccessor.statesByType();
        for (BlockState state : block.getStateDefinition().getPossibleStates()) {
            statesByType.put(state, lodestone);
        }
        LODESTONE_BLOCKS.add(block);
    }
}
