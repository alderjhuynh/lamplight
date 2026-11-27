package com.aura.lamplight.folio;

import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record MulticastBarPayload(UUID bar, int sections) implements CustomPacketPayload {

    public static final Type<MulticastBarPayload> TYPE = new Type<>(Folio.id("multicast_bar"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MulticastBarPayload> CODEC =
        StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, MulticastBarPayload::bar,
            ByteBufCodecs.VAR_INT, MulticastBarPayload::sections,
            MulticastBarPayload::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void initialize() {
        PayloadTypeRegistry.clientboundPlay().register(TYPE, CODEC);
    }
}
