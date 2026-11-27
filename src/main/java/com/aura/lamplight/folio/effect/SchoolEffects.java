package com.aura.lamplight.folio.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.jspecify.annotations.Nullable;

import com.aura.lamplight.folio.spell.SpellSchool;

public final class SchoolEffects {
    private SchoolEffects() {}

    public static void onSpellDamage(
        final SpellSchool school,
        final @Nullable LivingEntity caster,
        final LivingEntity target,
        final int stacks
    ) {
        SchoolStatus status = SchoolStatus.of(school);
        LivingEntity recipient = status.recipient() == SchoolStatus.Recipient.CASTER ? caster : target;
        if (recipient != null) {
            grant(status, recipient, caster, stacks);
        }
    }

    public static boolean grant(
        final SchoolStatus status,
        final LivingEntity recipient,
        final @Nullable LivingEntity source,
        final int stacks
    ) {
        if (!(recipient.level() instanceof ServerLevel level)
            || !recipient.isAlive()
            || recipient.isSpectator()
            || recipient instanceof ArmorStand) {
            return false;
        }

        MobEffectInstance current = recipient.getEffect(status.effect());
        StackRules.Result result = StackRules.apply(
            current == null ? 0 : StackRules.stacksFromAmplifier(current.getAmplifier()),
            current == null ? 0 : current.getDuration(),
            stacks);

        boolean cannotFreeze = status == SchoolStatus.CHILL && recipient.is(FolioEffectTags.FREEZE_IMMUNE);
        int newStacks = cannotFreeze ? Math.min(result.stacks(), StackRules.MAX_STACKS - 1) : result.stacks();

        MobEffectInstance next = new MobEffectInstance(
            status.effect(), result.duration(), newStacks - 1, false, true, true);
        if (!recipient.canBeAffected(next)) {
            return false;
        }

        if (newStacks >= StackRules.MAX_STACKS && status.triggersAtMax()) {
            recipient.removeEffect(status.effect());
            EffectPayoffs.trigger(level, status, recipient, source);
            return true;
        }

        EffectSources.remember(recipient, status, source);
        recipient.forceAddEffect(next, source);
        return true;
    }

    public static int stacks(final LivingEntity entity, final SchoolStatus status) {
        MobEffectInstance instance = entity.getEffect(status.effect());
        return instance == null ? 0 : StackRules.stacksFromAmplifier(instance.getAmplifier());
    }
}
