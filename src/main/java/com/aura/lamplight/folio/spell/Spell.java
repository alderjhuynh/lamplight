package com.aura.lamplight.folio.spell;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public abstract class Spell {

    private final Identifier id;
    private final Component name;
    private final SpellSchool school;
    private final int manaCost;
    private final int castTime;
    private final int cooldownTicks;
    private final boolean interruptible;
    private final boolean channeled;

    protected Spell(final Identifier id, final Component name, final SpellSchool school) {
        this(id, name, school, 10, 20, 40, true, false);
    }

    protected Spell(
        final Identifier id,
        final Component name,
        final SpellSchool school,
        final int manaCost,
        final int castTime,
        final int cooldownTicks,
        final boolean interruptible
    ) {
        this(id, name, school, manaCost, castTime, cooldownTicks, interruptible, false);
    }

    protected Spell(
        final Identifier id,
        final Component name,
        final SpellSchool school,
        final int manaCost,
        final int castTime,
        final int cooldownTicks,
        final boolean interruptible,
        final boolean channeled
    ) {
        this.id = id;
        this.name = name;
        this.school = school;
        this.manaCost = manaCost;
        this.castTime = castTime;
        this.cooldownTicks = cooldownTicks;
        this.interruptible = interruptible;
        this.channeled = channeled;
    }

    public final SpellSchool school() {
        return school;
    }

    public final Identifier id() {
        return id;
    }

    public final Component name() {
        return name;
    }

    public final int manaCost() {
        return manaCost;
    }

    public final int castTime() {
        return castTime;
    }

    public final int cooldownTicks() {
        return cooldownTicks;
    }

    public final boolean interruptible() {
        return interruptible;
    }

    public final boolean channeled() {
        return channeled;
    }

    public abstract void cast(ServerLevel level, Player caster, ItemStack sourceItem);

    public void castTick(final ServerLevel level, final Player caster, final ItemStack sourceItem) {
    }

    @Override
    public String toString() {
        return "Spell[" + id + "]";
    }
}
