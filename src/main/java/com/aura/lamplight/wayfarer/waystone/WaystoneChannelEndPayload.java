package com.aura.lamplight.wayfarer.waystone;

import com.aura.lamplight.wayfarer.Wayfarer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record WaystoneChannelEndPayload(boolean succeeded)
		implements CustomPacketPayload {

	public static final Type<WaystoneChannelEndPayload> TYPE =
			new Type<>(Wayfarer.id("waystone_channel_end"));

	public static final StreamCodec<RegistryFriendlyByteBuf, WaystoneChannelEndPayload> CODEC =
			StreamCodec.composite(
					ByteBufCodecs.BOOL, WaystoneChannelEndPayload::succeeded,
					WaystoneChannelEndPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void initialize() {
		PayloadTypeRegistry.clientboundPlay().register(TYPE, CODEC);
	}
}
