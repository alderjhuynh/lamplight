package com.aura.lamplight.folio.spell;

import com.aura.lamplight.folio.Folio;
import com.aura.lamplight.folio.server.GravityWellHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class GravityWellSpell extends Spell {

    public static final Identifier ID = Folio.id("gravity_well");

    public GravityWellSpell() {
        super(ID, Component.translatable("spell.lamplight.gravity_well"), SpellSchool.ARCANE, 30, 0, 240, true);
    }

    @Override
    public void cast(final ServerLevel level, final Player caster, final ItemStack scrollItem) {
        if (caster instanceof ServerPlayer serverPlayer) {
            GravityWellHandler.activate(level, serverPlayer);
        }
    }
}
