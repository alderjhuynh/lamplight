package com.aura.lamplight.folio.spell;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Base class for every spell that a scroll (or, later, a spellbook page)
 * can hold. To add a new spell:
 *
 * <ol>
 *   <li>Create a class that extends {@code Spell}, e.g. {@code FireballSpell}.</li>
 *   <li>Give it a public no-args constructor that calls
 *       {@code super(id, displayName)} - see {@link FrostBoltSpell} or
 *       {@link SlowingAuraSpell} for examples.</li>
 *   <li>Implement {@link #cast(ServerLevel, Player, ItemStack)} with the
 *       actual effect.</li>
 *   <li>Register an instance of it in {@link Spells}.</li>
 * </ol>
 *
 * <p>Spells are looked up at cast time by the {@link Identifier} stored in
 * an item's {@link com.aura.lamplight.folio.component.ModDataComponents#SPELL}
 * component, so any number of spells can share the same underlying item
 * without new item registrations.</p>
 */
public abstract class Spell {

    private final Identifier id;
    private final Component name;

    protected Spell(final Identifier id, final Component name) {
        this.id = id;
        this.name = name;
    }

    /** The unique id used to store/identify this spell, e.g. {@code lamplight:frost_bolt}. */
    public final Identifier id() {
        return id;
    }

    /** Display name shown in tooltips (e.g. "Frost Bolt"). */
    public final Component name() {
        return name;
    }

    /**
     * Performs the actual spell effect. Only ever called server-side.
     *
     * <p>Casting from a scroll is always free and instant right now -
     * mana cost, cast time, and spellbook storage are handled elsewhere
     * once those systems exist.</p>
     *
     * @param level      the server level the caster is in
     * @param caster     the player who used the scroll
     * @param scrollItem the exact scroll stack that was used (not yet shrunk)
     */
    public abstract void cast(ServerLevel level, Player caster, ItemStack scrollItem);

    @Override
    public String toString() {
        return "Spell[" + id + "]";
    }
}
