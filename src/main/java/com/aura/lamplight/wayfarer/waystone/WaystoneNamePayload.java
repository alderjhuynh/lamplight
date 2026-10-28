package com.aura.lamplight.wayfarer.waystone;

import com.aura.lamplight.wayfarer.Wayfarer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public record WaystoneNamePayload(BlockPos stone, String name)
		implements CustomPacketPayload {

	private static final int MAX_NAME_LENGTH = 48;
	private static final double MAX_NAMING_DISTANCE_SQ = 6.5 * 6.5;

	public static final Type<WaystoneNamePayload> TYPE =
			new Type<>(Wayfarer.id("waystone_name"));

	public static final StreamCodec<RegistryFriendlyByteBuf, WaystoneNamePayload> CODEC =
			StreamCodec.composite(
					BlockPos.STREAM_CODEC, WaystoneNamePayload::stone,
					ByteBufCodecs.stringUtf8(MAX_NAME_LENGTH * 4), WaystoneNamePayload::name,
					WaystoneNamePayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void initialize() {
		PayloadTypeRegistry.serverboundPlay().register(TYPE, CODEC);
		ServerPlayNetworking.registerGlobalReceiver(TYPE, (payload, context) -> {
			context.server().execute(() -> handle(context.player(), payload));
		});
	}

	private static void handle(ServerPlayer player, WaystoneNamePayload payload) {
		if (!(player.level() instanceof ServerLevel level)) {
			return;
		}
		BlockPos stone = payload.stone();
		double dx = player.getX() - (stone.getX() + 0.5);
		double dy = player.getY() - (stone.getY() + 1.5);
		double dz = player.getZ() - (stone.getZ() + 0.5);
		if (dx * dx + dy * dy + dz * dz > MAX_NAMING_DISTANCE_SQ) {
			return;
		}
		if (!WaystoneBlock.isLitLower(level.getBlockState(stone))) {
			return;
		}
		WaystoneNetwork network = WaystoneNetwork.get(level);
		if (!network.contains(level.dimension(), stone)) {
			return;
		}
		network.rename(level.dimension(), stone, sanitize(payload.name()));
	}

	private static String sanitize(String raw) {
		String cleaned = raw.replace("\u00a7", "");
		cleaned = cleaned.replaceAll("\\s+", " ").trim();
		if (cleaned.length() > MAX_NAME_LENGTH) {
			cleaned = cleaned.substring(0, MAX_NAME_LENGTH);
		}
		return cleaned;
	}
}
