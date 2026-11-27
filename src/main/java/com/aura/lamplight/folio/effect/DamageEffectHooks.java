package com.aura.lamplight.folio.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public final class DamageEffectHooks {
    private DamageEffectHooks() {}

    public static float modifyIncomingDamage(
        final ServerLevel level,
        final LivingEntity target,
        final DamageSource source,
        final float incoming
    ) {
        if (incoming <= 0.0F
            || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
            || target.isInvulnerableTo(level, source)) {
            return incoming;
        }

        if (source.getDirectEntity() instanceof DamageRecorder recorder) {
            recorder.recordDamaged(target);
        }

        float damage = incoming;
        boolean melee = isMelee(source);

        // judgemet
        if (source.getEntity() instanceof LivingEntity attacker && (melee || source.is(FolioEffectTags.RANGED))) {
            int judgement = SchoolEffects.stacks(attacker, SchoolStatus.JUDGEMENT);
            damage *= reduction(judgement, EffectTuning.JUDGEMENT_REDUCTION_PER_STACK);
        }

        // void
        if (melee && damage > 0.0F) {
            int voidStacks = SchoolEffects.stacks(target, SchoolStatus.VOID);
            if (voidStacks > 0) {
                damage *= 1.0F + voidStacks * EffectTuning.VOID_BONUS_PER_STACK;
                target.removeEffect(FolioEffects.VOID);
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, target.getX(), target.getY(0.5D), target.getZ(),
                    6 + voidStacks * 3, 0.25D, 0.35D, 0.25D, 0.08D);
            }
        }

        // resil
        if (damage > 0.0F) {
            int resilience = SchoolEffects.stacks(target, SchoolStatus.RESILIENCE);
            if (resilience > 0) {
                damage *= reduction(resilience, EffectTuning.RESILIENCE_REDUCTION_PER_STACK);
                target.removeEffect(FolioEffects.RESILIENCE);
                level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.PLAYERS, 0.9F, 0.7F);
                level.sendParticles(ParticleTypes.COMPOSTER, target.getX(), target.getY(0.5D), target.getZ(),
                    4 + resilience * 2, 0.4D, 0.5D, 0.4D, 0.02D);
            }
        }

        if (damage > 0.0F) {
            ShockChain.onDamaged(level, target, source, damage);
        }
        return damage;
    }

    private static boolean isMelee(final DamageSource source) {
        return source.getEntity() != null
            && source.getDirectEntity() == source.getEntity()
            && source.is(FolioEffectTags.MELEE);
    }

    private static float reduction(final int stacks, final float perStack) {
        return Math.max(0.0F, 1.0F - stacks * perStack);
    }
}
