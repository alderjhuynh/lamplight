package com.aura.lamplight.folio.spell;

import com.aura.lamplight.folio.Folio;
import com.aura.lamplight.folio.temporal.TemporalField;
import com.aura.lamplight.folio.temporal.TemporalFields;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * A field-type spell: a sphere of slowed time around the caster. Every
 * other player inside it experiences the world as if the global tick rate
 * had been set to 10, while the caster is left untouched. Built on top of
 * {@link TemporalFields}, so it can be reused for any other "time
 * dilation" effect.
 */
public final class GravityWellSpell extends Spell {

    public static final Identifier ID = Folio.id("gravity_well");

    private static final float RADIUS = 6.0F;
    private static final int DURATION_TICKS = 200; // 10 seconds
    private static final float TARGET_TICK_RATE = 10.0F;

    public GravityWellSpell() {
        super(ID, Component.translatable("spell.lamplight.gravity_well"));
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
            0.8F,
            0.5F
        );

        level.sendParticles(
            ParticleTypes.PORTAL,
            caster.getX(),
            caster.getY() + 1.0D,
            caster.getZ(),
            40,
            RADIUS * 0.6D,
            0.4D,
            RADIUS * 0.6D,
            0.05D
        );

        TemporalFields.add(
            TemporalField.builder(level)
                .follow(caster)
                .radius(RADIUS)
                .tickRate(TARGET_TICK_RATE)
                .duration(DURATION_TICKS)
                .filter(entity -> entity instanceof Player && !entity.getUUID().equals(caster.getUUID()))
                .visual(ParticleTypes.PORTAL, 5, 30)
                .build()
        );
    }
}
