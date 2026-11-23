package com.aura.lamplight.nullbloom;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

public final class GodsBargain {
	private static final Identifier HEART_TAKEN = Nullbloom.id("god_heart_taken");
	private static final double HEART = 2.0;
	private static final double MIN_MAX_HEALTH_AFTER = 2.0;

	private GodsBargain() {
	}

	public static final String DEAL_KEY = "lamplight_god_deal";

	public static boolean hasAccepted(ServerPlayer player) {
		if (player.entityTags().contains(DEAL_KEY)) {
			return true;
		}
		AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
		return maxHealth != null && maxHealth.hasModifier(HEART_TAKEN);
	}

	public static boolean hasAccepted(net.minecraft.world.entity.player.Player player) {
		if (player.entityTags().contains(DEAL_KEY)) {
			return true;
		}
		AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
		return maxHealth != null && maxHealth.hasModifier(HEART_TAKEN);
	}

	public static void strike(ServerPlayer player) {
		if (hasAccepted(player)) {
			return;
		}
		player.addTag(DEAL_KEY);
		AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
		if (maxHealth != null && maxHealth.getValue() - HEART >= MIN_MAX_HEALTH_AFTER) {
			maxHealth.addPermanentModifier(new AttributeModifier(HEART_TAKEN, -HEART, AttributeModifier.Operation.ADD_VALUE));
			player.setHealth(Math.min(player.getHealth(), player.getMaxHealth()));
		} else if (maxHealth != null) {
			maxHealth.addPermanentModifier(new AttributeModifier(HEART_TAKEN, 0.0, AttributeModifier.Operation.ADD_VALUE));
		}
		ItemStack flower = new ItemStack(NullbloomBlocks.NULLBLOOM_ITEM);
		if (!player.getInventory().add(flower)) {
			player.drop(flower, false);
		}
	}
}
