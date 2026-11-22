package com.aura.lamplight.nullbloom;

import java.util.UUID;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class NullbloomDimension {
	private NullbloomDimension() {
	}

	public static final ResourceKey<Level> LEVEL = ResourceKey.create(Registries.DIMENSION, Nullbloom.id("nullbloom"));


	public static final int MIN_Y = 0;
	public static final int HEIGHT = 128;
	public static final int SURFACE_Y = 64;
	public static final int ALTAR_SIZE_XZ = 16;
	public static final int ALTAR_ORIGIN_X = -ALTAR_SIZE_XZ / 2;
	public static final int ALTAR_ORIGIN_Z = -ALTAR_SIZE_XZ / 2;
	public static final int ALTAR_GROUND_LAYER = 0;

	public static Vec3 spawnPosition() {
		return new Vec3(-0.5, SURFACE_Y + 1, -0.5);
	}

	public static boolean is(@Nullable Level level) {
		return level != null && level.dimension() == LEVEL;
	}

	public static boolean is(Entity entity) {
		return is(entity.level());
	}

	public static boolean isBuildLocked(@Nullable Level level) {
		return is(level);
	}

	public static @Nullable TeleportTransition portalDestination(ServerLevel from, Entity entity) {
		ServerLevel target = from.getServer().getLevel(LEVEL);
		if (target == null) {
			Nullbloom.LOGGER.error("Dimension {} is not loaded; falling back to vanilla end portal behaviour", LEVEL.identifier());
			return null;
		}
		Vec3 pos = spawnPosition();
		return new TeleportTransition(
			target,
			pos,
			Vec3.ZERO,
			entity.getYRot(),
			0.0F,
			TeleportTransition.PLAY_PORTAL_SOUND.then(TeleportTransition.PLACE_PORTAL_TICKET)
		);
	}

	public static @Nullable UUID privateOwner(Entity entity) {
		if (entity instanceof PlayerPrivate marked) {
			return marked.privateOwner();
		}
		if (entity instanceof Player player) {
			return player.getUUID();
		}
		if (entity instanceof Projectile projectile && projectile.getOwner() instanceof Player owner) {
			return owner.getUUID();
		}
		if (entity instanceof ItemEntity item && item.getOwner() instanceof Player thrower) {
			return thrower.getUUID();
		}
		return null;
	}

	public static boolean canPerceive(ServerPlayer viewer, Entity target) {
		if (target == viewer) {
			return true;
		}
		UUID owner = privateOwner(target);
		return owner == null || owner.equals(viewer.getUUID());
	}

	public static boolean areStrangers(Entity a, Entity b) {
		return a != b && a instanceof Player && b instanceof Player && is(a) && is(b);
	}
}
