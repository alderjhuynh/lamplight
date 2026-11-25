package com.aura.lamplight.nullbloom;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record GodCallPayload(int action) implements CustomPacketPayload {
	public static final int START_FULL = 0;
	public static final int START_BRIEF = 1;
	public static final int END = 2;
	public static final int ABORT = 3;

	public static final Type<GodCallPayload> TYPE = new Type<>(Nullbloom.id("god_call"));

	public static final StreamCodec<RegistryFriendlyByteBuf, GodCallPayload> CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT, GodCallPayload::action,
		GodCallPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
