package com.aura.lamplight.folio.spell;

import com.aura.lamplight.folio.Folio;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * A field-type spell: drops a lingering {@link AreaEffectCloud} at the
 * caster's feet that continuously slows anything standing inside it.
 *
 * <p>{@code AreaEffectCloud} already re-applies its effects to anything
 * inside it roughly once a second for as long as it lives, which is
 * exactly the "continuous aura" behaviour we want, so no custom entity
 * is needed for this spell. Use this as a template for other "field"
 * or "aura" spells.</p>
 */
public final class SlowingAuraSpell extends Spell {

    public static final Identifier ID = Folio.id("slowing_aura");

    private static final float RADIUS = 3.5F;
    private static final int DURATION_TICKS = 160; // 8 seconds total lifetime
    private static final int EFFECT_DURATION_TICKS = 40; // must outlast the ~20-tick reapplication gap
    private static final int SLOW_AMPLIFIER = 1; // Slowness II

    public SlowingAuraSpell() {
        super(ID, Component.translatable("spell.lamplight.slowing_aura"));
    }

    @Override
    public void cast(final ServerLevel level, final Player caster, final ItemStack scrollItem) {
        level.playSound(
            null,
            caster.getX(),
            caster.getY(),
            caster.getZ(),
            SoundEvents.EVOKER_CAST_SPELL,
            SoundSource.PLAYERS,
            0.7F,
            0.8F
        );

        AreaEffectCloud cloud = new AreaEffectCloud(level, caster.getX(), caster.getY(), caster.getZ());
        cloud.setOwner(caster);
        cloud.setRadius(RADIUS);
        cloud.setDuration(DURATION_TICKS);
        cloud.setWaitTime(0); // active immediately, no vanilla-lingering-potion windup
        cloud.setCustomParticle(ParticleTypes.SNOWFLAKE);
        cloud.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, EFFECT_DURATION_TICKS, SLOW_AMPLIFIER));

        level.addFreshEntity(cloud);
    }
}
