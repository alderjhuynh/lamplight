package com.aura.lamplight.folio;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record CastingStatePayload(String spell, int totalTicks, int remainingTicks, boolean channeled)
    implements CustomPacketPayload {

    public static final Type<CastingStatePayload> TYPE = new Type<>(Folio.id("casting_state"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CastingStatePayload> CODEC =
        StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, CastingStatePayload::spell,
            ByteBufCodecs.VAR_INT, CastingStatePayload::totalTicks,
            ByteBufCodecs.VAR_INT, CastingStatePayload::remainingTicks,
            ByteBufCodecs.BOOL, CastingStatePayload::channeled,
            CastingStatePayload::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void initialize() {
        PayloadTypeRegistry.clientboundPlay().register(TYPE, CODEC);
    }
}
