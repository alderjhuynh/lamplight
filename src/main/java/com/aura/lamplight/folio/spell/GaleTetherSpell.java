package com.aura.lamplight.folio.spell;

import com.aura.lamplight.folio.Folio;
import com.aura.lamplight.folio.server.WindChainHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class GaleTetherSpell extends Spell {

    public static final Identifier ID = Folio.id("gale_tether");

    private static final int BUFF_DURATION_TICKS = 240; // 12 seconds

    public GaleTetherSpell() {
        super(ID, Component.translatable("spell.lamplight.gale_tether"), SpellSchool.WIND, 14, 15, 160, true);
    }

    @Override
    public void cast(final ServerLevel level, final Player caster, final ItemStack scrollItem) {
        if (caster instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            WindChainHandler.buff(serverPlayer, BUFF_DURATION_TICKS);
        }

        level.playSound(
            null,
            caster.getX(),
            caster.getY(),
            caster.getZ(),
            SoundEvents.EVOKER_CAST_SPELL,
            SoundSource.PLAYERS,
            0.9F,
            1.3F
        );

        level.sendParticles(
            net.minecraft.core.particles.ParticleTypes.CLOUD,
            caster.getX(),
            caster.getY() + 1.0D,
            caster.getZ(),
            20,
            0.6D,
            0.4D,
            0.6D,
            0.05D
        );
    }
}
