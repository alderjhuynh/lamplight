package com.aura.lamplight.armory;

import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class RapierItem extends Item {
	public static final TagKey<Item> RAPIERS =
			TagKey.create(Registries.ITEM, Identifier.withDefaultNamespace("melee_weapons/rapiers"));

	public RapierItem(ToolMaterial material, float attackDamageBaseline, float attackSpeedBaseline, Properties properties) {
		super(properties.sword(material, attackDamageBaseline, attackSpeedBaseline));
	}

	public static boolean isRapier(@Nullable ItemStack stack) {
		return stack != null && stack.getItem() instanceof RapierItem;
	}

	@Override
	public InteractionResult use(final Level level, final Player player, final InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (hand == InteractionHand.MAIN_HAND
				&& player.getOffhandItem().has(DataComponents.BLOCKS_ATTACKS)
				&& !player.isSecondaryUseActive()) {
			return InteractionResult.PASS;
		}

		if (player.isUsingItem() || player.getCooldowns().isOnCooldown(stack)) {
			return InteractionResult.PASS;
		}

		player.startUsingItem(hand);
		if (player instanceof ServerPlayer serverPlayer) {
			RapierCombat.beginParry(serverPlayer, hand);
		}
		return InteractionResult.CONSUME;
	}

	@Override
	public int getUseDuration(final ItemStack stack, final LivingEntity user) {
		return RapierCombat.PARRY_TICKS;
	}

	@Override
	public void postHurtEnemy(final ItemStack stack, final LivingEntity target, final LivingEntity attacker) {
		super.postHurtEnemy(stack, target, attacker);
		if (attacker instanceof ServerPlayer player) {
			RapierCombat.onHit(player, target);
		}
	}

	@Override
	public float getAttackDamageBonus(final Entity victim, final float damage, final DamageSource damageSource) {
		if (damageSource.getEntity() instanceof Player attacker && RapierCombat.isThrustReady(attacker)) {
			return damage * RapierCombat.THRUST_DAMAGE_BONUS;
		}
		return super.getAttackDamageBonus(victim, damage, damageSource);
	}

	@Override
	@SuppressWarnings("deprecation")
	public void appendHoverText(
			final ItemStack stack,
			final Item.TooltipContext context,
			final TooltipDisplay display,
			final Consumer<Component> builder,
			final TooltipFlag flag
	) {
		super.appendHoverText(stack, context, display, builder, flag);
		builder.accept(Component.translatable("tooltip.lamplight.rapier.parry")
				.withStyle(style -> style.withColor(ChatFormatting.GRAY).withItalic(false)));
		builder.accept(Component.translatable("tooltip.lamplight.rapier.thrust")
				.withStyle(style -> style.withColor(ChatFormatting.GRAY).withItalic(false)));
	}
}
