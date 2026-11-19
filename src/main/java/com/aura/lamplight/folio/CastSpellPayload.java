package com.aura.lamplight.folio;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record CastSpellPayload(int spellIndex)
        implements CustomPacketPayload {
    public static final Type<CastSpellPayload> TYPE = new Type<>(Folio.id("cast_spell"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CastSpellPayload> CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, CastSpellPayload::spellIndex, CastSpellPayload::new);
    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
    public static void initialize() {
        PayloadTypeRegistry.serverboundPlay().register(TYPE, CODEC);
    }
}
