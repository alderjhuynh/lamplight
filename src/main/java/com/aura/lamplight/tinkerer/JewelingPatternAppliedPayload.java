package com.aura.lamplight.tinkerer;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public record JewelingPatternAppliedPayload(int containerId, Optional<Identifier> templateId)
		implements CustomPacketPayload {

	public static final Type<JewelingPatternAppliedPayload> TYPE =
			new Type<>(Tinkerer.id("jeweling_pattern_applied"));

	public static final StreamCodec<RegistryFriendlyByteBuf, JewelingPatternAppliedPayload> CODEC =
			StreamCodec.composite(
					ByteBufCodecs.VAR_INT, JewelingPatternAppliedPayload::containerId,
					ByteBufCodecs.optional(Identifier.STREAM_CODEC), JewelingPatternAppliedPayload::templateId,
					JewelingPatternAppliedPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void initialize() {
		PayloadTypeRegistry.clientboundPlay().register(JewelingPatternAppliedPayload.TYPE, CODEC);
	}
}
