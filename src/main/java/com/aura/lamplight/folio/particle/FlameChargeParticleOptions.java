package com.aura.lamplight.folio.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record FlameChargeParticleOptions(int entityId) implements ParticleOptions {

    public static final int FRAMES = 61;

    public static final MapCodec<FlameChargeParticleOptions> CODEC =
        RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("entity_id").forGetter(FlameChargeParticleOptions::entityId)
        ).apply(instance, FlameChargeParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FlameChargeParticleOptions> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.VAR_INT, FlameChargeParticleOptions::entityId,
            FlameChargeParticleOptions::new
        );

    @Override
    public ParticleType<?> getType() {
        return ModParticles.FLAME_CHARGE;
    }
}
