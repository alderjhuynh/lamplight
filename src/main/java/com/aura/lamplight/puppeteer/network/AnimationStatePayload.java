package com.aura.lamplight.puppeteer.network;

import com.aura.lamplight.puppeteer.Puppeteer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record AnimationStatePayload(int entityId, long startGameTime, float playbackRate, float measuredSpeed, List<Entry> entries)
    implements CustomPacketPayload {

    public record Entry(Identifier animationId, long startGameTime) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                Identifier.CODEC.fieldOf("animation").forGetter(Entry::animationId),
                Codec.LONG.fieldOf("start").forGetter(Entry::startGameTime)
            ).apply(instance, Entry::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC,
            Entry::animationId,
            ByteBufCodecs.VAR_LONG,
            Entry::startGameTime,
            Entry::new
        );
    }

    public static final CustomPacketPayload.Type<AnimationStatePayload> TYPE = new CustomPacketPayload.Type<>(Puppeteer.id("animation_state"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AnimationStatePayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT,
        AnimationStatePayload::entityId,
        ByteBufCodecs.VAR_LONG,
        AnimationStatePayload::startGameTime,
        ByteBufCodecs.FLOAT,
        AnimationStatePayload::playbackRate,
        ByteBufCodecs.FLOAT,
        AnimationStatePayload::measuredSpeed,
        ByteBufCodecs.collection(ArrayList::new, Entry.STREAM_CODEC),
        AnimationStatePayload::entries,
        AnimationStatePayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
