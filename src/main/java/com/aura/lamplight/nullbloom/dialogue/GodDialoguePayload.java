package com.aura.lamplight.nullbloom.dialogue;

import com.aura.lamplight.nullbloom.Nullbloom;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record GodDialoguePayload(int sessionId, List<GodLine> lines, boolean expectChoice) implements CustomPacketPayload {
	public static final Type<GodDialoguePayload> TYPE = new Type<>(Nullbloom.id("god_dialogue"));

	public static final StreamCodec<RegistryFriendlyByteBuf, GodDialoguePayload> CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT, GodDialoguePayload::sessionId,
		GodLine.STREAM_CODEC.apply(ByteBufCodecs.list(64)), GodDialoguePayload::lines,
		ByteBufCodecs.BOOL, GodDialoguePayload::expectChoice,
		GodDialoguePayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
