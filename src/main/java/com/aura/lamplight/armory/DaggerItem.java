package com.aura.lamplight.armory;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import org.jspecify.annotations.Nullable;

public class DaggerItem extends Item {
	public static final TagKey<Item> DAGGERS =
			TagKey.create(Registries.ITEM, Identifier.withDefaultNamespace("melee_weapons/daggers"));

	public static final int HIT_INVULNERABLE_TIME = 15;

	public DaggerItem(ToolMaterial material, float attackDamageBaseline, float attackSpeedBaseline, Properties properties) {
		super(properties.sword(material, attackDamageBaseline, attackSpeedBaseline));
	}

	@Override
	public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		super.postHurtEnemy(stack, target, attacker);
		if (target.invulnerableTime > HIT_INVULNERABLE_TIME) {
			target.invulnerableTime = HIT_INVULNERABLE_TIME;
		}
	}

	public static boolean isDagger(@Nullable ItemStack stack) {
		return stack != null && stack.getItem() instanceof DaggerItem;
	}

	public static boolean isDaggerAttack(@Nullable DamageSource source) {
		return source != null && isDagger(source.getWeaponItem());
	}

	public static boolean canSprintCrit(Player player, Entity target) {
		return player.isSprinting()
				&& !player.onClimbable()
				&& !player.isInWater()
				&& !player.isMobilityRestricted()
				&& !player.isPassenger()
				&& target instanceof LivingEntity;
	}
}
