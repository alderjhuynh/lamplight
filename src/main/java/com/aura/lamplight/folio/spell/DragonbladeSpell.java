package com.aura.lamplight.folio.spell;

import com.aura.lamplight.folio.Folio;
import com.aura.lamplight.folio.server.DragonbladeHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class DragonbladeSpell extends Spell {

    public static final Identifier ID = Folio.id("dragonblade");

    public DragonbladeSpell() {
        super(ID, Component.translatable("spell.lamplight.dragonblade"), SpellSchool.ARCANE, 30, 20, 240, true);
    }

    @Override
    public void cast(final ServerLevel level, final Player caster, final ItemStack scrollItem) {
        if (caster instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            DragonbladeHandler.activate(serverPlayer);
        }

        level.playSound(
            null,
            caster.getX(),
            caster.getY(),
            caster.getZ(),
            SoundEvents.EVOKER_CAST_SPELL,
            SoundSource.PLAYERS,
            1.0F,
            0.6F
        );
    }
}
