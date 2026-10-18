package com.aura.lamplight.puppeteer.network;

import com.aura.lamplight.puppeteer.Puppeteer;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record AnimationStatePayload(int entityId, Optional<Identifier> animationId, long startGameTime) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<AnimationStatePayload> TYPE = new CustomPacketPayload.Type<>(Puppeteer.id("animation_state"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AnimationStatePayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT,
        AnimationStatePayload::entityId,
        ByteBufCodecs.optional(Identifier.STREAM_CODEC),
        AnimationStatePayload::animationId,
        ByteBufCodecs.VAR_LONG,
        AnimationStatePayload::startGameTime,
        AnimationStatePayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
