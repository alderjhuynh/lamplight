package com.aura.lamplight.ambrosia.block;

import com.aura.lamplight.ambrosia.Ambrosia;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleType;

public final class PotParticles {
    private PotParticles() {}

    public static final ParticleType<ColorParticleOption> COOKING_POT_BUBBLE = Registry.register(
            BuiltInRegistries.PARTICLE_TYPE, Ambrosia.id("cooking_pot_bubble"),
            FabricParticleTypes.complex(
                    type -> ColorParticleOption.codec((ParticleType<ColorParticleOption>) type),
                    type -> ColorParticleOption.streamCodec((ParticleType<ColorParticleOption>) type)));

    public static void register() {
        Ambrosia.LOGGER.info("Registered ambrosia particles");
    }
}
