package com.aura.lamplight.folio.particle;

import com.aura.lamplight.folio.Folio;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.SimpleParticleType;

public final class ModParticles {
    private ModParticles() {}

    public static final SimpleParticleType CHAIN_LINK = Registry.register(
        BuiltInRegistries.PARTICLE_TYPE, Folio.id("chain_link"),
        FabricParticleTypes.simple());

    public static void register() {
        Folio.LOGGER.info("Registered folio particles");
    }
}
