package com.aura.lamplight.nullbloom.dialogue;

import com.aura.lamplight.nullbloom.Nullbloom;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record GodDialogueResponsePayload(int sessionId, int kind) implements CustomPacketPayload {
	public static final int YES = 1;
	public static final int NO = 2;
	public static final int DONE = 3;

	public static final Type<GodDialogueResponsePayload> TYPE = new Type<>(Nullbloom.id("god_dialogue_response"));

	public static final StreamCodec<RegistryFriendlyByteBuf, GodDialogueResponsePayload> CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT, GodDialogueResponsePayload::sessionId,
		ByteBufCodecs.VAR_INT, GodDialogueResponsePayload::kind,
		GodDialogueResponsePayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
