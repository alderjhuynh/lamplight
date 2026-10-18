package com.aura.lamplight.folio.spell;

import com.aura.lamplight.folio.Folio;
import com.aura.lamplight.folio.entity.FrostBoltEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;

/**
 * A simple bolt-type spell: launches a single {@link FrostBoltEntity}
 * from the caster, following their aim.
 *
 * <p>Use this as a template for other "shoot a projectile" spells.</p>
 */
public final class FrostBoltSpell extends Spell {

    public static final Identifier ID = Folio.id("frost_bolt");

    private static final float PROJECTILE_SPEED = 1.6F;
    private static final float PROJECTILE_INACCURACY = 0.6F;

    public FrostBoltSpell() {
        super(ID, Component.translatable("spell.lamplight.frost_bolt"));
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
            0.7F,
            1.4F
        );

        Projectile.spawnProjectileFromRotation(FrostBoltEntity::new, level, scrollItem, caster, 0.0F, PROJECTILE_SPEED, PROJECTILE_INACCURACY);
    }
}
