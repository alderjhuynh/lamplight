package com.aura.lamplight.folio;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record OpenSpellbookPayload() implements CustomPacketPayload {
    public static final Type<OpenSpellbookPayload> TYPE = new Type<>(Folio.id("open_spellbook"));
    public static final OpenSpellbookPayload INSTANCE = new OpenSpellbookPayload();
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenSpellbookPayload> CODEC =
        StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void initialize() {
        PayloadTypeRegistry.serverboundPlay().register(TYPE, CODEC);
    }
}
