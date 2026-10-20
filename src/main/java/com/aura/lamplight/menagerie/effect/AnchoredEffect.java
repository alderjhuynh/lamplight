package com.aura.lamplight.menagerie.effect;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * A cold, clinging weight. Soul sand and soul soil grip a little harder while
 * this is active. The movement penalty itself is an attribute modifier so it
 * scales cleanly with the effect level.
 */
public class AnchoredEffect extends MobEffect {
    private static final int SOUL_SLOWNESS_DURATION = 30;

    public AnchoredEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int amplification) {
        return tickCount % 20 == 0;
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity mob, int amplification) {
        if (!isOnSoulBlock(mob)) {
            return true;
        }

        MobEffectInstance current = mob.getEffect(MobEffects.SLOWNESS);
        if (current == null || current.getAmplifier() < 0) {
            mob.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, SOUL_SLOWNESS_DURATION, 0, true, false, true));
        }
        return true;
    }

    private static boolean isOnSoulBlock(LivingEntity mob) {
        return mob.getBlockStateOn().is(BlockTags.SOUL_SPEED_BLOCKS)
                || mob.level().getBlockState(mob.getOnPos()).is(BlockTags.SOUL_SPEED_BLOCKS);
    }
}
