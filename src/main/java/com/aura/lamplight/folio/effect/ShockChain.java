package com.aura.lamplight.folio.effect;

import com.aura.lamplight.folio.entity.BloodThrallEntity;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

final class ShockChain {
    private ShockChain() {}

    static void onDamaged(
        final ServerLevel level,
        final LivingEntity origin,
        final DamageSource source,
        final float damage
    ) {
        int stacks = SchoolEffects.stacks(origin, SchoolStatus.SHOCK);
        if (stacks == 0 || source.is(FolioEffectTags.NO_SHOCK_CHAIN)
            || !(source.getEntity() instanceof LivingEntity attacker)
            || attacker == origin
            || level.getRandom().nextFloat() >= stacks * EffectTuning.SHOCK_CHANCE_PER_STACK) {
            return;
        }

        Vec3 from = origin.position().add(0.0D, origin.getBbHeight() * 0.5D, 0.0D);
        int limit = StackRules.shockChainTargets(stacks);
        float chained = damage * EffectTuning.SHOCK_CHAIN_DAMAGE_SHARE;
        EffectScheduler.schedule(() -> arc(level, origin, attacker, from, limit, chained));
    }

    private static void arc(
        final ServerLevel level,
        final LivingEntity origin,
        final LivingEntity attacker,
        final Vec3 from,
        final int limit,
        final float damage
    ) {
        double radius = EffectTuning.SHOCK_CHAIN_RADIUS;
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class,
                origin.getBoundingBox().inflate(radius),
                candidate -> canChainTo(origin, attacker, candidate)
                    && candidate.position().distanceToSqr(origin.position()) <= radius * radius)
            .stream()
            .sorted(Comparator.comparingDouble((LivingEntity candidate) -> candidate.position().distanceToSqr(origin.position())))
            .limit(limit)
            .toList();

        for (LivingEntity target : targets) {
            Vec3 to = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
            if (SideDamage.deal(level, target, FolioDamageTypes.SHOCK_CHAIN, attacker, damage)) {
                drawArc(level, from, to);
            }
        }
        if (!targets.isEmpty()) {
            level.playSound(null, from.x, from.y, from.z,
                SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.35F, 1.7F);
        }
    }

    private static boolean canChainTo(final LivingEntity origin, final LivingEntity attacker, final LivingEntity candidate) {
        if (candidate == origin || candidate == attacker || !candidate.isAlive() || candidate.isSpectator()
            || candidate instanceof ArmorStand || attacker.isAlliedTo(candidate)) {
            return false;
        }
        if (candidate instanceof OwnableEntity pet && pet.getOwnerReference() != null
            && pet.getOwnerReference().getUUID().equals(attacker.getUUID())) {
            return false;
        }
        if (candidate instanceof BloodThrallEntity thrall && thrall.isOwnedBy(attacker)) {
            return false;
        }
        return !(candidate instanceof Player victim && attacker instanceof Player player && !player.canHarmPlayer(victim));
    }

    private static void drawArc(final ServerLevel level, final Vec3 from, final Vec3 to) {
        Vec3 step = to.subtract(from);
        int points = Math.max(2, (int) (step.length() * 5.0D));
        for (int i = 0; i <= points; i++) {
            Vec3 at = from.add(step.scale(i / (double) points));
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, at.x, at.y, at.z, 1, 0.04D, 0.04D, 0.04D, 0.0D);
        }
    }
}
