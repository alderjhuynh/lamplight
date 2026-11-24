package com.aura.lamplight.armory;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ArmorVisibilityTogglePayload(int bit) implements CustomPacketPayload {
	public static final Type<ArmorVisibilityTogglePayload> TYPE = new Type<>(Armory.id("armor_visibility_toggle"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ArmorVisibilityTogglePayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, ArmorVisibilityTogglePayload::bit,
			ArmorVisibilityTogglePayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
