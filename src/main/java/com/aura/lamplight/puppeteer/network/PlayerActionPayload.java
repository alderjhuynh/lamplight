package com.aura.lamplight.puppeteer.network;

import com.aura.lamplight.puppeteer.Puppeteer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record PlayerActionPayload(String action) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PlayerActionPayload> TYPE = new CustomPacketPayload.Type<>(Puppeteer.id("player_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerActionPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8,
        PlayerActionPayload::action,
        PlayerActionPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
