package com.aura.lamplight.folio.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record BlastwaveParticleOptions(float size) implements ParticleOptions {

    public static final MapCodec<BlastwaveParticleOptions> CODEC =
        RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.fieldOf("size").forGetter(BlastwaveParticleOptions::size)
        ).apply(instance, BlastwaveParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BlastwaveParticleOptions> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.FLOAT, BlastwaveParticleOptions::size,
            BlastwaveParticleOptions::new
        );

    @Override
    public ParticleType<?> getType() {
        return ModParticles.BLASTWAVE;
    }
}
