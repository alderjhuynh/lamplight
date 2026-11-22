package com.aura.lamplight.nullbloom;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public final class NullbloomBlocks {
	private NullbloomBlocks() {
	}

	public static final ResourceKey<Block> NULLBLOOM_KEY =
		ResourceKey.create(Registries.BLOCK, Nullbloom.id("nullbloom"));
	public static final ResourceKey<Item> NULLBLOOM_ITEM_KEY =
		ResourceKey.create(Registries.ITEM, Nullbloom.id("nullbloom"));

	public static final Block NULLBLOOM = Registry.register(
		BuiltInRegistries.BLOCK,
		NULLBLOOM_KEY,
		new NullbloomBlock(BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_GRAY)
			.noCollision()
			.instabreak()
			.sound(SoundType.GRASS)
			.offsetType(BlockBehaviour.OffsetType.XZ)
			.pushReaction(PushReaction.DESTROY)
			.setId(NULLBLOOM_KEY)));

	public static final Item NULLBLOOM_ITEM = Registry.register(
		BuiltInRegistries.ITEM,
		NULLBLOOM_ITEM_KEY,
		new BlockItem(NULLBLOOM, new Item.Properties().useBlockDescriptionPrefix().setId(NULLBLOOM_ITEM_KEY)));

	public static void register() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.NATURAL_BLOCKS)
			.register(output -> output.accept(NULLBLOOM_ITEM));
	}
}
