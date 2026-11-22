package com.aura.lamplight.folio.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ShieldParticleOptions(int entityId, int mode) implements ParticleOptions {

    public static final int REFRESH = 0;
    public static final int COLLAPSE = 1;
    public static final int IMPACT = 2;

    public static final MapCodec<ShieldParticleOptions> CODEC =
        RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("entity_id").forGetter(ShieldParticleOptions::entityId),
            Codec.INT.optionalFieldOf("mode", REFRESH).forGetter(ShieldParticleOptions::mode)
        ).apply(instance, ShieldParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShieldParticleOptions> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ShieldParticleOptions::entityId,
            ByteBufCodecs.VAR_INT, ShieldParticleOptions::mode,
            ShieldParticleOptions::new
        );

    @Override
    public ParticleType<?> getType() {
        return ModParticles.SHIELD;
    }
}
