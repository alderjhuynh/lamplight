package com.aura.lamplight.folio;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record DragonbladeStatePayload(boolean active, int durationTicks)
        implements CustomPacketPayload {

    public static final Type<DragonbladeStatePayload> TYPE =
            new Type<>(Folio.id("dragonblade_state"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DragonbladeStatePayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, DragonbladeStatePayload::active,
                    ByteBufCodecs.VAR_INT, DragonbladeStatePayload::durationTicks,
                    DragonbladeStatePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void initialize() {
        PayloadTypeRegistry.clientboundPlay().register(TYPE, CODEC);
    }
}
