package com.aura.lamplight.folio.effect;

import com.aura.lamplight.folio.spell.Spell;
import com.aura.lamplight.folio.spell.SpellSchool;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;

public final class SwirlReset {
    private SwirlReset() {}

    public static boolean tryConsume(final ServerPlayer player, final Spell spell) {
        if (spell.school() != SpellSchool.WIND
            || SchoolEffects.stacks(player, SchoolStatus.SWIRL) < StackRules.MAX_STACKS) {
            return false;
        }
        player.removeEffect(FolioEffects.SWIRL);
        player.level().sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY(0.5D), player.getZ(),
            20, 0.5D, 0.5D, 0.5D, 0.08D);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.4F);
        return true;
    }
}
