package com.aura.lamplight.folio.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record FireShockwaveParticleOptions(float size) implements ParticleOptions {

    public static final MapCodec<FireShockwaveParticleOptions> CODEC =
        RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.fieldOf("size").forGetter(FireShockwaveParticleOptions::size)
        ).apply(instance, FireShockwaveParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FireShockwaveParticleOptions> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.FLOAT, FireShockwaveParticleOptions::size,
            FireShockwaveParticleOptions::new
        );

    @Override
    public ParticleType<?> getType() {
        return ModParticles.BLASTWAVE;
    }
}
