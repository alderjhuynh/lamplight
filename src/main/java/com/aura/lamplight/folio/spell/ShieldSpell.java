package com.aura.lamplight.folio.spell;

import com.aura.lamplight.folio.Folio;
import com.aura.lamplight.folio.server.FolioManaHandler;
import com.aura.lamplight.folio.server.ShieldHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class ShieldSpell extends ChargeSpell {

    public static final Identifier ID = Folio.id("shield");

    public static final int MAX_ACTIVE_TICKS = 20 * 30;
    public static final double MANA_PER_TICK = 0.1D;
    public static final double MANA_PER_PROJECTILE = 2.0D;
    public static final double MANA_PER_DAMAGE = 0.5D;

    public ShieldSpell() {
        super(ID, Component.translatable("spell.lamplight.shield"), SpellSchool.HOLY, 12, 100, false);
    }

    @Override
    public int maxChargeTicks() {
        return MAX_ACTIVE_TICKS;
    }

    @Override
    public int maxHoldTicks() {
        return MAX_ACTIVE_TICKS;
    }

    @Override
    public float chargeProgress(final Player caster, final int heldTicks) {
        return Math.max(0.0F, 1.0F - heldTicks / (float) MAX_ACTIVE_TICKS);
    }

    @Override
    public void chargeStart(
        final ServerLevel level,
        final Player caster,
        final ItemStack source,
        final CastOrigin origin
    ) {
        ShieldHandler.begin(level, caster, origin);
    }

    @Override
    public boolean chargeTick(
        final ServerLevel level,
        final Player caster,
        final ItemStack source,
        final CastOrigin origin,
        final int heldTicks
    ) {
        if (origin == CastOrigin.SPELLBOOK
            && (ShieldHandler.isBroken(caster) || !FolioManaHandler.spend(caster, MANA_PER_TICK))) {
            if (caster instanceof ServerPlayer player) {
                player.sendSystemMessage(
                    Component.translatable("message.lamplight.spellbook.no_mana").withStyle(ChatFormatting.RED), true);
            }
            return false;
        }
        return true;
    }

    @Override
    public void chargeRelease(
        final ServerLevel level,
        final Player caster,
        final ItemStack source,
        final CastOrigin origin,
        final int heldTicks
    ) {
        ShieldHandler.end(caster);
    }

    @Override
    public void chargeCancel(final ServerLevel level, final Player caster, final CastOrigin origin) {
        ShieldHandler.end(caster);
    }
}
