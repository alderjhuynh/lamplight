package com.aura.lamplight.wayfarer.waystone;

import com.aura.lamplight.wayfarer.Wayfarer;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.SimpleParticleType;

public final class ModParticles {
    private ModParticles() {}

    public static final SimpleParticleType WAYSTONE_TELEPORT = Registry.register(
        BuiltInRegistries.PARTICLE_TYPE, Wayfarer.id("waystone_teleport"),
        FabricParticleTypes.simple());

    public static void register() {
        Wayfarer.LOGGER.info("Registered wayfarer particles");
    }
}
