package com.aura.lamplight.folio.effect;

import com.aura.lamplight.folio.entity.BloodThrallEntity;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

final class EffectPayoffs {
    private EffectPayoffs() {}

    static void trigger(
        final ServerLevel level,
        final SchoolStatus status,
        final LivingEntity target,
        final @Nullable LivingEntity source
    ) {
        switch (status) {
            case CHILL -> freeze(level, target, source);
            case BURN -> explode(level, target, source);
            case BLEED -> rupture(level, target, source);
            case JUDGEMENT -> retribution(level, target, source);
            default -> throw new IllegalStateException(status + " has no payoff");
        }
    }

    private static void freeze(final ServerLevel level, final LivingEntity target, final @Nullable LivingEntity source) {
        target.addEffect(new MobEffectInstance(
            FolioEffects.FROZEN, EffectTuning.FREEZE_TICKS, 0, false, true, true), source);
        level.playSound(null, target.getX(), target.getY(), target.getZ(),
            SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 0.6F);
        level.sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY(0.5D), target.getZ(),
            40, target.getBbWidth() * 0.5D, target.getBbHeight() * 0.4D, target.getBbWidth() * 0.5D, 0.08D);
    }

    private static void explode(final ServerLevel level, final LivingEntity target, final @Nullable LivingEntity source) {
        double x = target.getX();
        double y = target.getY(0.5D);
        double z = target.getZ();
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(ParticleTypes.FLAME, x, y, z, 50, 0.3D, 0.4D, 0.3D, 0.18D);
        level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.4F, 1.15F);
        SideDamage.deal(level, target, FolioDamageTypes.BURN_EXPLOSION, source, EffectTuning.EXPLODE_DAMAGE);
    }

    private static void rupture(final ServerLevel level, final LivingEntity target, final @Nullable LivingEntity source) {
        double x = target.getX();
        double y = target.getY(0.5D);
        double z = target.getZ();
        level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, x, y, z, 24, 0.35D, 0.45D, 0.35D, 0.2D);
        level.sendParticles(ParticleTypes.CRIMSON_SPORE, x, y, z, 60, 0.6D, 0.6D, 0.6D, 0.05D);
        level.playSound(null, x, y, z, SoundEvents.ZOMBIE_INFECT, SoundSource.PLAYERS, 1.0F, 0.6F);
        SideDamage.deal(level, target, FolioDamageTypes.RUPTURE, source, EffectTuning.RUPTURE_DAMAGE);
        if (source != null) {
            BloodThrallEntity.summon(level, source, target);
        }
    }

    private static void retribution(final ServerLevel level, final LivingEntity target, final @Nullable LivingEntity source) {
        double x = target.getX();
        double z = target.getZ();
        double top = target.getY() + target.getBbHeight() + 8.0D;
        for (double y = target.getY(); y <= top; y += 0.25D) {
            level.sendParticles(ParticleTypes.END_ROD, x, y, z, 2, 0.08D, 0.05D, 0.08D, 0.005D);
        }
        level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 1.0F, 0.9F, 0.55F),
            x, target.getY(0.5D), z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.playSound(null, x, target.getY(), z, SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 1.2F, 1.3F);
        level.playSound(null, x, target.getY(), z, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.9F, 1.6F);
        SideDamage.deal(level, target, FolioDamageTypes.RETRIBUTION, source, EffectTuning.RETRIBUTION_DAMAGE);
    }
}
