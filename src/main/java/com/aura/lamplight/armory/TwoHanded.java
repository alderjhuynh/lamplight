package com.aura.lamplight.armory;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class TwoHanded {
	private TwoHanded() {}

	public static boolean isTwoHanded(ItemStack stack) {
		return !stack.isEmpty() && stack.get(ModDataComponents.TWO_HANDED) == Boolean.TRUE;
	}

	public static boolean isOffhandSuppressed(Player player) {
		return isTwoHanded(player.getMainHandItem());
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(TwoHanded::sweep);
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			ItemStack probe = new ItemStack(com.aura.lamplight.armory.ModItems.NETHERITE_GREATSWORD);
			Armory.LOGGER.info("[twohanded-debug] greatsword default stack has component: {}", probe.get(ModDataComponents.TWO_HANDED));
			Armory.LOGGER.info("[twohanded-debug] bow default stack has component: {}", probe.get(ModDataComponents.TWO_HANDED));
		});
	}

	private static void sweep(MinecraftServer server) {
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			ItemStack offhand = player.getItemBySlot(EquipmentSlot.OFFHAND);
			if (isTwoHanded(offhand)) {
				player.getInventory().removeItemNoUpdate(Inventory.SLOT_OFFHAND);
				player.getInventory().placeItemBackInInventory(offhand);
			} else if (isOffhandSuppressed(player)
					&& player.isUsingItem()
					&& player.getUsedItemHand() == InteractionHand.OFF_HAND) {
				player.stopUsingItem();
			}
		}
	}
}
