package com.aura.lamplight.wayfarer.waystone;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class WaystoneChanneling {
	private WaystoneChanneling() {
	}

	private static final int CHANNEL_TICKS = 60;
	private static final double MAX_DRIFT_SQ = 2.0 * 2.0;

	private record Channel(ResourceKey<Level> destination, BlockPos pillar,
	                       double anchorX, double anchorY, double anchorZ, int ticksLeft) {
	}

	private static final Map<UUID, Channel> CHANNELING = new HashMap<>();

	public static void initialize() {
		ServerTickEvents.END_SERVER_TICK.register(WaystoneChanneling::tick);
	}

	public static boolean isChanneling(ServerPlayer player) {
		return CHANNELING.containsKey(player.getUUID());
	}

	public static void begin(ServerPlayer player, ResourceKey<Level> destination, BlockPos pillar) {
		Channel channel = new Channel(destination, pillar, player.getX(), player.getY(), player.getZ(), CHANNEL_TICKS);
		CHANNELING.put(player.getUUID(), channel);
		ServerPlayNetworking.send(player, new WaystoneChannelBeginPayload(pillar, CHANNEL_TICKS));
	}

	private static void tick(MinecraftServer server) {
		if (CHANNELING.isEmpty()) {
			return;
		}
		Iterator<Map.Entry<UUID, Channel>> iterator = CHANNELING.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<UUID, Channel> entry = iterator.next();
			ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
			Channel channel = entry.getValue();
			if (player == null) {
				iterator.remove();
				continue;
			}
			if (!stillGathered(player, channel, server)) {
				iterator.remove();
				ServerPlayNetworking.send(player, new WaystoneChannelEndPayload(false));
				continue;
			}
			if (channel.ticksLeft() <= 0) {
				iterator.remove();
				arrive(player, channel);
				continue;
			}
			entry.setValue(new Channel(channel.destination(), channel.pillar(),
					channel.anchorX(), channel.anchorY(), channel.anchorZ(), channel.ticksLeft() - 1));
		}
	}

	private static boolean stillGathered(ServerPlayer player, Channel channel, MinecraftServer server) {
		if (!player.isAlive() || player.isPassenger() || player.isSleeping()) {
			return false;
		}
		double dx = player.getX() - channel.anchorX();
		double dy = player.getY() - channel.anchorY();
		double dz = player.getZ() - channel.anchorZ();
		if (dx * dx + dy * dy + dz * dz > MAX_DRIFT_SQ) {
			return false;
		}
		return true;
	}

	private static void arrive(ServerPlayer player, Channel channel) {
		ServerLevel destination = player.level().getServer().getLevel(channel.destination());
		if (destination == null) {
			return;
		}
		BlockPos pillar = channel.pillar();
		BlockState base = destination.getBlockState(pillar);
		if (!WaystoneBlock.isLit(base)) {
			ServerPlayNetworking.send(player, new WaystoneChannelEndPayload(false));
			lightWentOut(destination, pillar);
			return;
		}
		BlockPos stand = standSpot(destination, pillar, WaystoneBlock.facing(destination, pillar));
		if (stand == null) {
			ServerPlayNetworking.send(player, new WaystoneChannelEndPayload(false));
			lightWentOut(destination, pillar);
			return;
		}

		ServerLevel origin = player.level();
		origin.sendParticles(ModParticles.WAYSTONE_TELEPORT,
				player.getX(), player.getY() + 0.3, player.getZ(),
				40, 0.6, 1.0, 0.6, 0.06);

		ServerPlayNetworking.send(player, new WaystoneChannelEndPayload(true));

		float yaw = WaystoneBlock.facing(destination, pillar).getOpposite().toYRot();
		player.teleportTo(destination, stand.getX() + 0.5, stand.getY(), stand.getZ() + 0.5,
				java.util.Set.of(), yaw, player.getXRot(), false);

		destination.sendParticles(ModParticles.WAYSTONE_TELEPORT,
				stand.getX() + 0.5, stand.getY() + 1.2, stand.getZ() + 0.5,
				24, 0.5, 0.8, 0.5, 0.04);
		destination.playSound(null, stand, SoundEvents.PLAYER_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
	}

	private static void lightWentOut(ServerLevel destination, BlockPos pillar) {
		destination.sendParticles(ParticleTypes.SMOKE,
				pillar.getX() + 0.5, pillar.getY() + 2.2, pillar.getZ() + 0.5,
				12, 0.3, 0.4, 0.3, 0.01);
		destination.playSound(null, pillar, SoundEvents.FIRE_EXTINGUISH,
				SoundSource.BLOCKS, 0.4F, 1.4F);
	}

	private static BlockPos standSpot(ServerLevel level, BlockPos pillar, Direction facing) {
		List<Direction> candidates = List.of(
				facing, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
		BlockPos above = pillar.above();
		for (Direction direction : candidates) {
			BlockPos feet = above.relative(direction);
			if (clearAt(level, feet) && clearAt(level, feet.above())) {
				return feet;
			}
		}
		return null;
	}

	private static boolean clearAt(ServerLevel level, BlockPos pos) {
		return level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
	}
}
