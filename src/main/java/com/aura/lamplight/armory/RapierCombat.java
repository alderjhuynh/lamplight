package com.aura.lamplight.armory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class RapierCombat {
	private RapierCombat() {}

	public static final int PARRY_TICKS = 10;
	public static final int PARRY_WHIFF_COOLDOWN_TICKS = 90;
	public static final int PARRY_SUCCESS_COOLDOWN_TICKS = 8;
	public static final double PARRY_HOP_STRENGTH = 0.45D;
	public static final double PARRY_HOP_LIFT = 0.2D;

	public static final int STAGGER_TICKS = 20;
	public static final int STAGGER_SLOWNESS_AMPLIFIER = 4;
	public static final double STAGGER_KNOCKBACK = 0.6D;
	public static final int RIPOSTE_TICKS = 60;

	public static final int THRUST_EVERY = 3;
	public static final int COMBO_TIMEOUT_TICKS = 60;
	public static final float THRUST_DAMAGE_BONUS = 0.6F;
	public static final double THRUST_REACH_BONUS = 1.0D;

	public static final Identifier THRUST_REACH_ID = Armory.id("rapier_thrust_reach");

	private static final class State {
		long parryEndsAt = -1L;
		boolean parryLanded;
		Item parryItem;

		long riposteUntil = -1L;

		int hits;
		long lastHitAt = Long.MIN_VALUE;
	}

	private static final Map<UUID, State> STATES = new ConcurrentHashMap<>();

	private static State state(Player player) {
		return STATES.computeIfAbsent(player.getUUID(), id -> new State());
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(RapierCombat::tick);
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> STATES.remove(handler.getPlayer().getUUID()));
	}

	public static boolean isThrustReady(Player player) {
		AttributeInstance range = player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
		return range != null && range.hasModifier(THRUST_REACH_ID);
	}

	public static boolean hasRiposte(Player player) {
		if (!(player instanceof ServerPlayer) || !RapierItem.isRapier(player.getMainHandItem())) {
			return false;
		}
		State s = STATES.get(player.getUUID());
		return s != null && s.riposteUntil >= player.level().getGameTime();
	}

	public static void beginParry(ServerPlayer player, InteractionHand hand) {
		State s = state(player);
		if (s.parryEndsAt >= 0L && !s.parryLanded && s.parryItem != null) {
			player.getCooldowns().addCooldown(new ItemStack(s.parryItem), PARRY_WHIFF_COOLDOWN_TICKS);
		}
		s.parryEndsAt = player.level().getGameTime() + PARRY_TICKS;
		s.parryLanded = false;
		s.parryItem = player.getItemInHand(hand).getItem();

		hopBack(player);
		player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.PLAYER_ATTACK_WEAK, SoundSource.PLAYERS, 0.8F, 1.6F);
	}

	private static void hopBack(ServerPlayer player) {
		if (player.isPassenger() || player.isInWater() || player.isFallFlying() || player.onClimbable()) {
			return;
		}
		double yaw = Math.toRadians(player.getYRot());
		double backX = Math.sin(yaw);
		double backZ = -Math.cos(yaw);
		Vec3 motion = player.getDeltaMovement();
		double y = player.onGround() ? PARRY_HOP_LIFT : motion.y;
		player.setSprinting(false);
		player.setDeltaMovement(backX * PARRY_HOP_STRENGTH, y, backZ * PARRY_HOP_STRENGTH);
		player.hurtMarked = true;
	}

	public static boolean tryParry(ServerPlayer victim, ServerLevel level, DamageSource source) {
		State s = STATES.get(victim.getUUID());
		if (s == null || s.parryEndsAt < 0L || s.parryLanded) {
			return false;
		}
		long now = level.getGameTime();
		if (now > s.parryEndsAt) {
			return false;
		}
		ItemStack parryStack = victim.getUseItem();
		if (!victim.isUsingItem() || !RapierItem.isRapier(parryStack)) {
			return false;
		}

		if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
				|| source.is(DamageTypeTags.BYPASSES_SHIELD)
				|| source.is(DamageTypeTags.IS_EXPLOSION)
				|| source.is(DamageTypes.THORNS)) {
			return false;
		}
		Entity direct = source.getDirectEntity();
		if (direct == null) {
			return false;
		}

		Vec3 origin = source.getSourcePosition() != null ? source.getSourcePosition() : direct.position();
		Vec3 toSource = new Vec3(origin.x - victim.getX(), 0.0D, origin.z - victim.getZ());
		if (toSource.lengthSqr() > 1.0E-6D) {
			Vec3 facing = Vec3.directionFromRotation(0.0F, victim.getYHeadRot());
			if (facing.dot(toSource.normalize()) <= 0.0D) {
				return false;
			}
		}

		s.parryLanded = true;
		s.parryEndsAt = -1L;
		s.parryItem = null;
		s.riposteUntil = now + RIPOSTE_TICKS;
		victim.getCooldowns().addCooldown(parryStack, PARRY_SUCCESS_COOLDOWN_TICKS);

		parryEffects(victim, level);

		if (direct == source.getEntity() && direct instanceof LivingEntity attacker) {
			stagger(victim, attacker, source);
		}
		return true;
	}

	private static void parryEffects(ServerPlayer victim, ServerLevel level) {
		Vec3 at = victim.getEyePosition().add(victim.getLookAngle().scale(0.9D)).add(0.0D, -0.25D, 0.0D);
		level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 1.0F, 1.0F, 1.0F),
				at.x, at.y, at.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
		level.sendParticles(ParticleTypes.CRIT, at.x, at.y, at.z, 12, 0.25D, 0.25D, 0.25D, 0.35D);
		level.sendParticles(ParticleTypes.ELECTRIC_SPARK, at.x, at.y, at.z, 8, 0.2D, 0.2D, 0.2D, 0.4D);
		level.playSound(null, at.x, at.y, at.z, SoundEvents.SHIELD_BLOCK.value(), SoundSource.PLAYERS, 1.0F, 1.6F);
		level.playSound(null, at.x, at.y, at.z, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.9F);
	}

	private static void stagger(ServerPlayer victim, LivingEntity attacker, DamageSource source) {
		attacker.knockback(STAGGER_KNOCKBACK, victim.getX() - attacker.getX(), victim.getZ() - attacker.getZ(), source, 0.0F);
		attacker.hurtMarked = true;
		attacker.stopUsingItem();
		attacker.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, STAGGER_TICKS, STAGGER_SLOWNESS_AMPLIFIER, false, true));
		if (attacker instanceof Mob mob) {
			mob.getNavigation().stop();
		}
		if (attacker instanceof Player other) {
			other.resetAttackStrengthTicker();
		}
	}

	public static void onHit(ServerPlayer player, LivingEntity target) {
		State s = state(player);
		long now = player.level().getGameTime();
		boolean wasThrust = isThrustReady(player);

		s.riposteUntil = -1L;

		if (now - s.lastHitAt > COMBO_TIMEOUT_TICKS) {
			s.hits = 0;
		}
		if (wasThrust) {
			s.hits = 0;
			thrustEffects(player, target);
		} else {
			s.hits++;
		}
		s.lastHitAt = now;
		syncThrustModifier(player, s);
	}

	private static void thrustEffects(ServerPlayer player, LivingEntity target) {
		if (!(player.level() instanceof ServerLevel level)) {
			return;
		}
		Vec3 at = target.getBoundingBox().getCenter();
		level.sendParticles(ParticleTypes.CRIT, at.x, at.y, at.z, 10, 0.2D, 0.2D, 0.2D, 0.3D);
		level.playSound(null, at.x, at.y, at.z, SoundEvents.PLAYER_ATTACK_KNOCKBACK, SoundSource.PLAYERS, 0.8F, 1.5F);
	}

	private static void syncThrustModifier(ServerPlayer player, State s) {
		AttributeInstance range = player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
		if (range == null) {
			return;
		}
		boolean want = s.hits >= THRUST_EVERY - 1 && RapierItem.isRapier(player.getMainHandItem());
		boolean has = range.hasModifier(THRUST_REACH_ID);
		if (want && !has) {
			range.addTransientModifier(new AttributeModifier(THRUST_REACH_ID, THRUST_REACH_BONUS, AttributeModifier.Operation.ADD_VALUE));
		} else if (!want && has) {
			range.removeModifier(THRUST_REACH_ID);
		}
	}

	private static void tick(MinecraftServer server) {
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			tickPlayer(player);
		}
	}

	private static void tickPlayer(ServerPlayer player) {
		State s = STATES.get(player.getUUID());
		if (s == null) {
			return;
		}
		if (!player.isAlive()) {
			STATES.remove(player.getUUID());
			return;
		}
		long now = player.level().getGameTime();

		if (s.parryEndsAt >= 0L && (now > s.parryEndsAt
				|| !player.isUsingItem() || !RapierItem.isRapier(player.getUseItem()))) {
			if (!s.parryLanded && s.parryItem != null) {
				player.getCooldowns().addCooldown(new ItemStack(s.parryItem), PARRY_WHIFF_COOLDOWN_TICKS);
			}
			s.parryEndsAt = -1L;
			s.parryLanded = false;
			s.parryItem = null;
		}

		if (s.riposteUntil >= 0L && now > s.riposteUntil) {
			s.riposteUntil = -1L;
		}
		if (s.hits > 0 && now - s.lastHitAt > COMBO_TIMEOUT_TICKS) {
			s.hits = 0;
		}

		syncThrustModifier(player, s);
	}
}
