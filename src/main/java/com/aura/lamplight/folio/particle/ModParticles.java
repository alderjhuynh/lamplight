package com.aura.lamplight.folio.particle;

import com.aura.lamplight.folio.Folio;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

public final class ModParticles {
    private ModParticles() {}

    public static final SimpleParticleType GRAVITY_WELL = Registry.register(
        BuiltInRegistries.PARTICLE_TYPE, Folio.id("gravity_well"),
        FabricParticleTypes.simple());

    public static final ParticleType<FlameChargeParticleOptions> FLAME_CHARGE = Registry.register(
        BuiltInRegistries.PARTICLE_TYPE, Folio.id("flame_charge"),
        FabricParticleTypes.complex(FlameChargeParticleOptions.CODEC, FlameChargeParticleOptions.STREAM_CODEC));

    public static final ParticleType<BlastwaveParticleOptions> BLASTWAVE = Registry.register(
        BuiltInRegistries.PARTICLE_TYPE, Folio.id("blastwave"),
        FabricParticleTypes.complex(BlastwaveParticleOptions.CODEC, BlastwaveParticleOptions.STREAM_CODEC));

    /** Smoke that is still burning; spawned after the fireball explodes. */
    public static final SimpleParticleType FIERY_SMOKE = Registry.register(
        BuiltInRegistries.PARTICLE_TYPE, Folio.id("fiery_smoke"),
        FabricParticleTypes.simple());

    public static void register() {
        Folio.LOGGER.info("Registered folio particles");
    }
}
