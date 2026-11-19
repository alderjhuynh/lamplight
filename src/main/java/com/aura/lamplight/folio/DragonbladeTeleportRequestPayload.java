package com.aura.lamplight.folio;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record DragonbladeTeleportRequestPayload(int entityId)
        implements CustomPacketPayload {

    public static final Type<DragonbladeTeleportRequestPayload> TYPE =
            new Type<>(Folio.id("dragonblade_teleport_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DragonbladeTeleportRequestPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, DragonbladeTeleportRequestPayload::entityId,
                    DragonbladeTeleportRequestPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void initialize() {
        PayloadTypeRegistry.serverboundPlay().register(TYPE, CODEC);
    }
}
