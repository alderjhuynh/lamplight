package com.aura.lamplight.arcanum.network;

import com.aura.lamplight.arcanum.Arcanum;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public final class ArcanumPackets {
    private ArcanumPackets() {}

    public record GlidePayload() implements CustomPacketPayload {
        public static final Type<GlidePayload> TYPE = new Type<>(Arcanum.id("glide"));
        public static final StreamCodec<RegistryFriendlyByteBuf, GlidePayload> CODEC =
                StreamCodec.unit(new GlidePayload());

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record JetBoostPayload(int ticks) implements CustomPacketPayload {
        public static final Type<JetBoostPayload> TYPE = new Type<>(Arcanum.id("jet_boost"));
        public static final StreamCodec<RegistryFriendlyByteBuf, JetBoostPayload> CODEC =
                StreamCodec.composite(ByteBufCodecs.VAR_INT, JetBoostPayload::ticks, JetBoostPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record AerodynamicsPayload(boolean boosted) implements CustomPacketPayload {
        public static final Type<AerodynamicsPayload> TYPE = new Type<>(Arcanum.id("aerodynamics"));
        public static final StreamCodec<RegistryFriendlyByteBuf, AerodynamicsPayload> CODEC =
                StreamCodec.composite(ByteBufCodecs.BOOL, AerodynamicsPayload::boosted, AerodynamicsPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record DashPayload(boolean backwards) implements CustomPacketPayload {
        public static final Type<DashPayload> TYPE = new Type<>(Arcanum.id("dash"));
        public static final StreamCodec<RegistryFriendlyByteBuf, DashPayload> CODEC =
                StreamCodec.composite(ByteBufCodecs.BOOL, DashPayload::backwards, DashPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record DoubleJumpPayload() implements CustomPacketPayload {
        public static final Type<DoubleJumpPayload> TYPE = new Type<>(Arcanum.id("double_jump"));
        public static final StreamCodec<RegistryFriendlyByteBuf, DoubleJumpPayload> CODEC =
                StreamCodec.unit(new DoubleJumpPayload());

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record FlowGlidePayload() implements CustomPacketPayload {
        public static final Type<FlowGlidePayload> TYPE = new Type<>(Arcanum.id("flow_glide"));
        public static final StreamCodec<RegistryFriendlyByteBuf, FlowGlidePayload> CODEC =
                StreamCodec.unit(new FlowGlidePayload());

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
