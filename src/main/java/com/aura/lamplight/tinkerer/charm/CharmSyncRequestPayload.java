package com.aura.lamplight.tinkerer.charm;

import com.aura.lamplight.tinkerer.Tinkerer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;

public record CharmSyncRequestPayload() implements CustomPacketPayload {

	public static final CharmSyncRequestPayload INSTANCE = new CharmSyncRequestPayload();

	public static final Type<CharmSyncRequestPayload> TYPE = new Type<>(Tinkerer.id("charm_sync_request"));
	public static final StreamCodec<RegistryFriendlyByteBuf, CharmSyncRequestPayload> CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
