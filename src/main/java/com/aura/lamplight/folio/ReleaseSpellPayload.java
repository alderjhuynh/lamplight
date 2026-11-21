package com.aura.lamplight.folio;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ReleaseSpellPayload() implements CustomPacketPayload {
    public static final Type<ReleaseSpellPayload> TYPE = new Type<>(Folio.id("release_spell"));
    public static final ReleaseSpellPayload INSTANCE = new ReleaseSpellPayload();
    public static final StreamCodec<RegistryFriendlyByteBuf, ReleaseSpellPayload> CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void initialize() {
        PayloadTypeRegistry.serverboundPlay().register(TYPE, CODEC);
    }
}
