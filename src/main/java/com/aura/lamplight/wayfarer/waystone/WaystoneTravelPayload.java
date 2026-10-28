package com.aura.lamplight.wayfarer.waystone;

import com.aura.lamplight.wayfarer.Wayfarer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public record WaystoneTravelPayload(ResourceKey<Level> destination, BlockPos pos)
		implements CustomPacketPayload {

	public static final Type<WaystoneTravelPayload> TYPE =
			new Type<>(Wayfarer.id("waystone_travel"));

	public static final StreamCodec<RegistryFriendlyByteBuf, WaystoneTravelPayload> CODEC =
			StreamCodec.composite(
					ResourceKey.streamCodec(Registries.DIMENSION), WaystoneTravelPayload::destination,
					BlockPos.STREAM_CODEC, WaystoneTravelPayload::pos,
					WaystoneTravelPayload::new);

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

	private static void handle(ServerPlayer player, WaystoneTravelPayload payload) {
		if (WaystoneChanneling.isChanneling(player) || player.isPassenger() || !player.isAlive()) {
			return;
		}
		if (!(player.level() instanceof ServerLevel level)) {
			return;
		}
		if (!level.dimension().equals(payload.destination())) {
			return;
		}
		MinecraftServer server = player.level().getServer();
		if (server == null) {
			return;
		}
		ServerLevel destinationLevel = server.getLevel(payload.destination());
		if (destinationLevel == null
				|| !WaystoneBlock.isLitLower(destinationLevel.getBlockState(payload.pos()))) {
			return;
		}
		WaystoneNetwork network = WaystoneNetwork.get(level);
		if (!network.contains(payload.destination(), payload.pos())) {
			return;
		}
		WaystoneChanneling.begin(player, payload.destination(), payload.pos());
	}
}
