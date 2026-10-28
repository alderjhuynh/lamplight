package com.aura.lamplight.wayfarer.waystone;

import com.aura.lamplight.wayfarer.Wayfarer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record WaystoneNamingPayload(BlockPos stone, String name)
		implements CustomPacketPayload {

	public static final Type<WaystoneNamingPayload> TYPE =
			new Type<>(Wayfarer.id("waystone_naming"));

	public static final StreamCodec<RegistryFriendlyByteBuf, WaystoneNamingPayload> CODEC =
			StreamCodec.composite(
					BlockPos.STREAM_CODEC, WaystoneNamingPayload::stone,
					ByteBufCodecs.stringUtf8(48), WaystoneNamingPayload::name,
					WaystoneNamingPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void initialize() {
		PayloadTypeRegistry.clientboundPlay().register(TYPE, CODEC);
	}
}
