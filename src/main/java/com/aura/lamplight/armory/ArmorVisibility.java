package com.aura.lamplight.armory;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;

import com.mojang.serialization.Codec;

@SuppressWarnings ("deprecation")
public final class ArmorVisibility {
	public static final int HEAD = 1;
	public static final int CHEST = 2;
	public static final int LEGS = 4;
	public static final int FEET = 8;
	public static final int BACK = 16;
	public static final int ALL = HEAD | CHEST | LEGS | FEET | BACK;

	public static final AttachmentType<Integer> VISIBILITY = AttachmentRegistry.<Integer>builder()
			.persistent(Codec.INT)
			.syncWith(net.minecraft.network.codec.ByteBufCodecs.INT, AttachmentSyncPredicate.all())
			.initializer(() -> ALL)
			.buildAndRegister(Armory.id("armor_visibility"));

	private ArmorVisibility() {
	}

	public static int mask(Player player) {
		Integer v = player.getAttached(VISIBILITY);
		return v == null ? ALL : v;
	}

	public static boolean isVisible(Player player, EquipmentSlot slot) {
		return (mask(player) & bitForEquipmentSlot(slot)) != 0;
	}

	public static boolean isBackVisible(Player player) {
		return (mask(player) & BACK) != 0;
	}

	public static boolean isVisibleBit(Player player, int bit) {
		return (mask(player) & bit) != 0;
	}

	public static void toggle(Player player, int bit) {
		int m = mask(player);
		if ((m & bit) != 0) {
			m &= ~bit;
		} else {
			m |= bit;
		}
		player.setAttached(VISIBILITY, m);
	}

	public static int bitForEquipmentSlot(EquipmentSlot slot) {
		return switch (slot) {
			case HEAD -> HEAD;
			case CHEST -> CHEST;
			case LEGS -> LEGS;
			case FEET -> FEET;
			default -> 0;
		};
	}
}
