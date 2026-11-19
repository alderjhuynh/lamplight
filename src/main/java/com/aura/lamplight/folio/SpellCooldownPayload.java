package com.aura.lamplight.folio;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SpellCooldownPayload(String spell, int remainingTicks) implements CustomPacketPayload {
    public static final Type<SpellCooldownPayload> TYPE = new Type<>(Folio.id("spell_cooldown"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SpellCooldownPayload> CODEC =
        StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SpellCooldownPayload::spell,
            ByteBufCodecs.VAR_INT, SpellCooldownPayload::remainingTicks,
            SpellCooldownPayload::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void initialize() {
        PayloadTypeRegistry.clientboundPlay().register(TYPE, CODEC);
    }
}
