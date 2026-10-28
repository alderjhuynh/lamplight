package com.aura.lamplight.menagerie.fishing;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;

public class MessageInABottleItem extends Item {

	private static final byte MAP_ZOOM = 1;
	private static final int SEARCH_RADIUS = 100;
	private static final String DECORATION_TRACK = "+";

	public MessageInABottleItem(final Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(final Level level, final Player player, final InteractionHand hand) {
		if (level instanceof ServerLevel serverLevel) {
			BlockPos target = serverLevel.findNearestMapStructure(
					StructureTags.ON_TREASURE_MAPS, player.blockPosition(), SEARCH_RADIUS, false);

			if (target == null) {
				return InteractionResult.FAIL;
			}

			ItemStack map = MapItem.create(serverLevel, target.getX(), target.getZ(), MAP_ZOOM, true, true);
			MapItem.renderBiomePreviewMap(serverLevel, map);
			MapItemSavedData.addTargetDecoration(map, target, DECORATION_TRACK, MapDecorationTypes.RED_X);
			map.set(DataComponents.ITEM_NAME, Component.translatable("filled_map.buried_treasure"));

			if (!player.hasInfiniteMaterials()) {
				player.getInventory().placeItemBackInInventory(new ItemStack(Items.GLASS_BOTTLE));
			}
			serverLevel.playSound(null, player.blockPosition(), SoundEvents.VILLAGER_WORK_CARTOGRAPHER,
					SoundSource.PLAYERS, 1.0F, 1.0F);
			return InteractionResult.SUCCESS.heldItemTransformedTo(map);
		}

		return InteractionResult.SUCCESS;
	}

	@Override
	public void appendHoverText(
			final ItemStack itemStack,
			final Item.TooltipContext context,
			final TooltipDisplay display,
			final Consumer<Component> builder,
			final TooltipFlag tooltipFlag
	) {
		builder.accept(Component.translatable("item.lamplight.message_in_a_bottle.tooltip")
				.withStyle(ChatFormatting.GRAY));
	}
}
