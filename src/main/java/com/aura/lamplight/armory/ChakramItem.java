package com.aura.lamplight.armory;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class ChakramItem extends Item {
	public static final TagKey<Item> CHAKRAMS =
			TagKey.create(Registries.ITEM, Identifier.withDefaultNamespace("melee_weapons/chakrams"));

	public static final float THROW_SPEED = (float) ThrownChakram.OUTBOUND_SPEED;
	public static final int THROW_COOLDOWN_TICKS = 8;

	public ChakramItem(ToolMaterial material, float attackDamageBaseline, float attackSpeedBaseline, Properties properties) {
		super(properties.sword(material, attackDamageBaseline, attackSpeedBaseline));
	}

	public static boolean isChakram(@Nullable ItemStack stack) {
		return stack != null && stack.getItem() instanceof ChakramItem;
	}

	@Override
	public InteractionResult use(final Level level, final Player player, final InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (hand == InteractionHand.MAIN_HAND
				&& player.getOffhandItem().has(DataComponents.BLOCKS_ATTACKS)
				&& !player.isSecondaryUseActive()) {
			return InteractionResult.PASS;
		}

		if (stack.nextDamageWillBreak()) {
			return InteractionResult.FAIL;
		}

		if (!(level instanceof ServerLevel serverLevel)) {
			return InteractionResult.SUCCESS;
		}

		int returnSlot = hand == InteractionHand.MAIN_HAND ? player.getInventory().getSelectedSlot() : Inventory.SLOT_OFFHAND;

		float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);

		player.awardStat(Stats.ITEM_USED.get(this));
		player.getCooldowns().addCooldown(stack, THROW_COOLDOWN_TICKS);
		stack.hurtWithoutBreaking(1, player);

		boolean infinite = player.hasInfiniteMaterials();
		ItemStack thrownStack = stack.consumeAndReturn(1, player);

		ThrownChakram chakram = new ThrownChakram(serverLevel, player, thrownStack);
		chakram.configure(damage, returnSlot, infinite);

		Vec3 look = player.getLookAngle();
		Projectile.spawnProjectile(chakram, serverLevel, thrownStack,
				c -> c.shoot(look.x, look.y, look.z, THROW_SPEED, 0.0F));

		serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
				ArmorySounds.CHAKRAM_THROW, SoundSource.PLAYERS, 0.9F, 0.95F + serverLevel.getRandom().nextFloat() * 0.15F);

		return InteractionResult.SUCCESS;
	}

}