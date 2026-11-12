package com.aura.lamplight.armory;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class DualWielded {
	private DualWielded() {}

	public static boolean isDualWielded(ItemStack stack) {
		return !stack.isEmpty() && stack.get(ModDataComponents.DUAL_WIELDED) == Boolean.TRUE;
	}

	public static boolean isGhost(ItemStack stack) {
		return !stack.isEmpty() && stack.get(ModDataComponents.DUAL_WIELD_GHOST) == Boolean.TRUE;
	}

	public static boolean isActive(Player player) {
		ItemStack mainhand = player.getMainHandItem();
		return !TwoHanded.isOffhandSuppressed(player) && !isGhost(mainhand) && isDualWielded(mainhand);
	}

	public static boolean hasGhost(Player player) {
		ItemStack offhand = player.getItemBySlot(EquipmentSlot.OFFHAND);
		return isGhost(offhand) && offhand.is(player.getMainHandItem().getItem());
	}

	public static ItemStack createGhost(ItemStack mainhand) {
		ItemStack ghost = mainhand.copy();
		ghost.setCount(1);
		ghost.set(ModDataComponents.DUAL_WIELD_GHOST, Boolean.TRUE);
		return ghost;
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(DualWielded::sweep);
	}

	private static void sweep(MinecraftServer server) {
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			syncOffhand(player);
		}
	}

	private static void syncOffhand(ServerPlayer player) {
		ItemStack offhand = player.getItemBySlot(EquipmentSlot.OFFHAND);
		boolean changed = false;

		if (!isActive(player)) {
			if (isGhost(offhand)) {
				player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
				changed = true;
			}
		} else {
			if (!isGhost(offhand) && !offhand.isEmpty()) {
				// the real offhand item has to make room for the ghost copy.
				player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
				player.getInventory().placeItemBackInInventory(offhand);
				offhand = ItemStack.EMPTY;
				changed = true;
			}

			ItemStack expected = createGhost(player.getMainHandItem());
			if (!ItemStack.matches(expected, offhand)) {
				player.setItemSlot(EquipmentSlot.OFFHAND, expected);
				changed = true;
			}
		}

		if (changed) {
			ItemStack synced = player.getItemBySlot(EquipmentSlot.OFFHAND);
			player.connection.send(new ClientboundSetEquipmentPacket(
					player.getId(),
					List.of(Pair.of(EquipmentSlot.OFFHAND, synced.copy()))
			));
			player.inventoryMenu.broadcastChanges();
		}
	}
}
