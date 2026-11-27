package com.aura.lamplight.folio.effect;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class DamageOverTimeEffect extends SchoolEffect {

    private final SchoolStatus status;
    private final ResourceKey<DamageType> damageType;
    private final float damagePerStack;
    private final int intervalTicks;

    public DamageOverTimeEffect(
        final int color,
        final SchoolStatus status,
        final ResourceKey<DamageType> damageType,
        final float damagePerStack,
        final int intervalTicks
    ) {
        super(MobEffectCategory.HARMFUL, color);
        this.status = status;
        this.damageType = damageType;
        this.damagePerStack = damagePerStack;
        this.intervalTicks = intervalTicks;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(final int tickCount, final int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(final ServerLevel level, final LivingEntity mob, final int amplifier) {
        if (mob.tickCount % intervalTicks == 0) {
            float damage = damagePerStack * StackRules.stacksFromAmplifier(amplifier);
            EffectScheduler.schedule(() -> {
                if (mob.isAlive() && mob.level() == level) {
                    SideDamage.deal(level, mob, damageType, EffectSources.applier(level, mob, status), damage);
                }
            });
        }
        return true;
    }
}
