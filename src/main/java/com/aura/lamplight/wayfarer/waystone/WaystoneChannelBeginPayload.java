package com.aura.lamplight.wayfarer.waystone;

import com.aura.lamplight.wayfarer.Wayfarer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record WaystoneChannelBeginPayload(BlockPos pillar, int totalTicks)
		implements CustomPacketPayload {

	public static final Type<WaystoneChannelBeginPayload> TYPE =
			new Type<>(Wayfarer.id("waystone_channel_begin"));

	public static final StreamCodec<RegistryFriendlyByteBuf, WaystoneChannelBeginPayload> CODEC =
			StreamCodec.composite(
					BlockPos.STREAM_CODEC, WaystoneChannelBeginPayload::pillar,
					ByteBufCodecs.VAR_INT, WaystoneChannelBeginPayload::totalTicks,
					WaystoneChannelBeginPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void initialize() {
		PayloadTypeRegistry.clientboundPlay().register(TYPE, CODEC);
	}
}
