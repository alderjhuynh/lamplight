package com.aura.lamplight.folio.effect;

import com.aura.lamplight.folio.server.FolioCastHandler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.phys.Vec3;

public class FrozenEffect extends MobEffect {

    public FrozenEffect(final int color) {
        super(MobEffectCategory.HARMFUL, color, ParticleTypes.SNOWFLAKE);
    }

    @Override
    public void onEffectStarted(final LivingEntity mob, final int amplifier) {
        if (mob.level().isClientSide()) {
            return;
        }
        Vec3 movement = mob.getDeltaMovement();
        mob.setDeltaMovement(0.0D, Math.min(0.0D, movement.y), 0.0D);
        mob.hurtMarked = true;
        interrupt(mob);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(final int tickCount, final int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(final ServerLevel level, final LivingEntity mob, final int amplifier) {
        interrupt(mob);
        if (mob.tickCount % 4 == 0) {
            level.sendParticles(ParticleTypes.SNOWFLAKE,
                mob.getX(), mob.getY() + mob.getBbHeight() * 0.5D, mob.getZ(),
                3, mob.getBbWidth() * 0.4D, mob.getBbHeight() * 0.35D, mob.getBbWidth() * 0.4D, 0.0D);
        }
        return true;
    }

    private static void interrupt(final LivingEntity mob) {
        if (mob.isUsingItem()) {
            mob.stopUsingItem();
        }
        if (mob instanceof ServerPlayer player && player.connection != null) {
            FolioCastHandler.cancelHold(player, true);
            FolioCastHandler.cancel(player, true);
        }
        if (mob instanceof Mob frozenMob) {
            frozenMob.getNavigation().stop();
        }
        if (mob instanceof Creeper creeper) {
            creeper.setSwellDir(-1);
        }
    }
}
