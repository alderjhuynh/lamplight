package com.aura.lamplight.tinkerer;

import com.aura.lamplight.tinkerer.charm.CharmAttachments;
import com.aura.lamplight.tinkerer.charm.CharmDeckLayout;
import com.aura.lamplight.tinkerer.charm.CharmSlotAttributes;
import com.aura.lamplight.tinkerer.charm.ModDataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Map;

public class CharmItem extends Item {

	private final String slotType;

	public CharmItem(final String slotType, final Properties properties) {
		super(properties);
		this.slotType = slotType;
	}

	public String slotType() {
		return this.slotType;
	}

	@Override
	public InteractionResult use(final Level level, final Player player, final InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!stack.has(ModDataComponents.CHARM)) {
			return InteractionResult.PASS;
		}
		Identifier targetSlot = firstMatchingSlot(player, stack);
		if (targetSlot == null) {
			return InteractionResult.PASS;
		}
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		ItemStack worn = CharmAttachments.charmIn(player, targetSlot);
		CharmAttachments.setCharm(player, targetSlot, stack.copyWithCount(1));
		if (!worn.isEmpty()) {
			player.addItem(worn);
		}
		stack.shrink(1);
		player.containerMenu.broadcastChanges();
		return InteractionResult.CONSUME;
	}

	private Identifier firstMatchingSlot(final Player player, final ItemStack stack) {
		boolean clientSide = player.level().isClientSide();
		Map<String, Integer> capacities = clientSide
				? CharmDeckLayout.clientCapacities()
				: CharmSlotAttributes.capacities(player);
		Map<String, Integer> acquired = clientSide
				? CharmDeckLayout.clientAcquired()
				: CharmSlotAttributes.acquired(player);
		Identifier fallback = null;
		for (CharmDeckLayout.Well well : CharmDeckLayout.wells(player, capacities, acquired)) {
			if (!well.slug().equals(this.slotType)) {
				continue;
			}
			if (CharmAttachments.charmIn(player, well.id()).isEmpty()) {
				return well.id();
			}
			if (fallback == null) {
				fallback = well.id();
			}
		}
		return fallback;
	}
}
