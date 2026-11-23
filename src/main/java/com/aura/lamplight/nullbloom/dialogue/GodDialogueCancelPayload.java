package com.aura.lamplight.nullbloom.dialogue;

import com.aura.lamplight.nullbloom.Nullbloom;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record GodDialogueCancelPayload(int sessionId) implements CustomPacketPayload {
	public static final Type<GodDialogueCancelPayload> TYPE = new Type<>(Nullbloom.id("god_dialogue_cancel"));

	public static final StreamCodec<RegistryFriendlyByteBuf, GodDialogueCancelPayload> CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT, GodDialogueCancelPayload::sessionId,
		GodDialogueCancelPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
