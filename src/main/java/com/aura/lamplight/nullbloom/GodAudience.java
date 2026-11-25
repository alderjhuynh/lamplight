package com.aura.lamplight.nullbloom;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public final class GodAudience {
	private static final Set<UUID> MEMBERS = new HashSet<>();

	private GodAudience() {
	}

	public static boolean isIn(@Nullable Entity entity) {
		return !MEMBERS.isEmpty() && entity instanceof Player player && MEMBERS.contains(player.getUUID());
	}

	public static boolean isIn(UUID id) {
		return MEMBERS.contains(id);
	}

	public static void enter(ServerPlayer player) {
		if (MEMBERS.add(player.getUUID())) {
			refreshTracking(player);
		}
	}

	public static void leave(ServerPlayer player) {
		if (MEMBERS.remove(player.getUUID())) {
			refreshTracking(player);
		}
	}

	public static void forget(UUID id) {
		MEMBERS.remove(id);
	}

	public static void clear() {
		MEMBERS.clear();
	}

	private static void refreshTracking(ServerPlayer player) {
		if (player.level() instanceof ServerLevel level && player.isAlive()) {
			level.getChunkSource().move(player);
		}
	}

	public static boolean hides(ServerPlayer viewer, Entity target) {
		if (MEMBERS.isEmpty()) {
			return false;
		}
		UUID owner = NullbloomDimension.privateOwner(target);
		if (owner == null || owner.equals(viewer.getUUID())) {
			return false;
		}
		return MEMBERS.contains(owner) || MEMBERS.contains(viewer.getUUID());
	}

	public static boolean areStrangers(Entity a, Entity b) {
		return !MEMBERS.isEmpty() && a != b && a instanceof Player && b instanceof Player && (isIn(a) || isIn(b));
	}

	public static boolean locksBuilding(@Nullable Entity entity) {
		return isIn(entity);
	}
}
