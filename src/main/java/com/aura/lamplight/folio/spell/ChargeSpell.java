package com.aura.lamplight.folio.spell;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public abstract class ChargeSpell extends Spell {

    protected ChargeSpell(
        final Identifier id,
        final Component name,
        final SpellSchool school,
        final int manaCost,
        final int cooldownTicks,
        final boolean interruptible
    ) {
        super(id, name, school, manaCost, 0, cooldownTicks, interruptible, false);
    }

    public abstract int maxChargeTicks();

    public int maxHoldTicks() {
        return Integer.MAX_VALUE;
    }

    public float chargeProgress(final Player caster, final int heldTicks) {
        return Math.min(1.0F, heldTicks / (float) Math.max(1, maxChargeTicks()));
    }

    public abstract void chargeStart(ServerLevel level, Player caster, ItemStack source, CastOrigin origin);

    public abstract boolean chargeTick(ServerLevel level, Player caster, ItemStack source, CastOrigin origin, int heldTicks);

    public abstract void chargeRelease(ServerLevel level, Player caster, ItemStack source, CastOrigin origin, int heldTicks);

    public abstract void chargeCancel(ServerLevel level, Player caster, CastOrigin origin);

    @Override
    public final void cast(final ServerLevel level, final Player caster, final ItemStack sourceItem) {
        chargeStart(level, caster, sourceItem, CastOrigin.SCROLL);
        chargeRelease(level, caster, sourceItem, CastOrigin.SCROLL, 0);
    }
}
