package com.aura.lamplight.armory;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

// client to server request to wield or sheathe back slot item
public record DrawSheathPayload(boolean hold) implements CustomPacketPayload {
	public static final Type<DrawSheathPayload> TYPE = new Type<>(Armory.id("draw_sheath"));
	public static final StreamCodec<RegistryFriendlyByteBuf, DrawSheathPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.BOOL, DrawSheathPayload::hold,
			DrawSheathPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
