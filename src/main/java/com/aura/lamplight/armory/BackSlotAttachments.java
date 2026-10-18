package com.aura.lamplight.armory;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;

import com.mojang.serialization.Codec;

public final class BackSlotAttachments {
	public static final AttachmentType<ItemStack> BACK_STACK = AttachmentRegistry.<ItemStack>builder()
			.persistent(ItemStack.OPTIONAL_CODEC)
			.syncWith(ItemStack.OPTIONAL_STREAM_CODEC, AttachmentSyncPredicate.all())
			.initializer(() -> ItemStack.EMPTY)
			.buildAndRegister(Armory.id("back_stack"));

	// whether the player is currently wielding the back slot item
	public static final AttachmentType<Boolean> HOLDING = AttachmentRegistry.<Boolean>builder()
			.persistent(Codec.BOOL)
			.syncWith(ByteBufCodecs.BOOL, AttachmentSyncPredicate.all())
			.initializer(() -> false)
			.buildAndRegister(Armory.id("holding"));

	private BackSlotAttachments() {
	}

	public static ItemStack getBackStack(net.minecraft.world.entity.player.Player player) {
		ItemStack stack = player.getAttached(BACK_STACK);
		return stack == null ? ItemStack.EMPTY : stack;
	}

	public static void setBackStack(net.minecraft.world.entity.player.Player player, ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			player.setAttached(BACK_STACK, ItemStack.EMPTY);
		} else {
			player.setAttached(BACK_STACK, stack);
		}
	}

	public static boolean isHolding(net.minecraft.world.entity.player.Player player) {
		Boolean holding = player.getAttached(HOLDING);
		return holding != null && holding;
	}

	public static void setHolding(net.minecraft.world.entity.player.Player player, boolean holding) {
		player.setAttached(HOLDING, holding);
	}
}
