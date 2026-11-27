package com.aura.lamplight.folio.spell;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public abstract class MultiCastSpell extends Spell {

    private final int casts;
    private final int castWindowTicks;

    protected MultiCastSpell(
        final Identifier id,
        final Component name,
        final SpellSchool school,
        final int manaCost,
        final int cooldownTicks,
        final boolean interruptible,
        final int casts,
        final int castWindowTicks
    ) {
        super(id, name, school, manaCost, 0, cooldownTicks, interruptible, false);
        if (casts < 2) {
            throw new IllegalArgumentException("Multi-cast spells need at least two casts");
        }
        if (castWindowTicks <= 0) {
            throw new IllegalArgumentException("Multi-cast window must be positive");
        }
        this.casts = casts;
        this.castWindowTicks = castWindowTicks;
    }

    @Override
    public final SpellType type() {
        return SpellType.MULTICAST;
    }

    public final int casts() {
        return casts;
    }

    public final int castWindowTicks() {
        return castWindowTicks;
    }

    public abstract void castAt(
        final ServerLevel level,
        final Player caster,
        final ItemStack sourceItem,
        final int castIndex
    );

    @Override
    public final void cast(final ServerLevel level, final Player caster, final ItemStack sourceItem) {
        castAt(level, caster, sourceItem, 0);
    }
}
