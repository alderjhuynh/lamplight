package com.aura.lamplight.folio;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SelectSpellPayload(int spellIndex) implements CustomPacketPayload {
    public static final Type<SelectSpellPayload> TYPE = new Type<>(Folio.id("select_spell"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SelectSpellPayload> CODEC =
        StreamCodec.composite(ByteBufCodecs.VAR_INT, SelectSpellPayload::spellIndex, SelectSpellPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void initialize() {
        PayloadTypeRegistry.serverboundPlay().register(TYPE, CODEC);
    }
}
