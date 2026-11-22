package com.aura.lamplight.folio.spell;

import com.aura.lamplight.folio.Folio;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public final class BlessingSpell extends Spell {

    public static final Identifier ID = Folio.id("blessing");

    private static final double RADIUS = 8.0D;
    private static final float HEAL_AMOUNT = 6.0F;
    private static final int REGENERATION_TICKS = 100;

    public BlessingSpell() {
        super(ID, Component.translatable("spell.lamplight.blessing"), SpellSchool.HOLY, 20, 20, 200, true);
    }

    @Override
    public void cast(final ServerLevel level, final Player caster, final ItemStack scrollItem) {
        AABB area = caster.getBoundingBox().inflate(RADIUS);
        List<Player> targets = level.getEntitiesOfClass(Player.class, area,
            player -> player.isAlive() && !player.isSpectator());

        for (Player target : targets) {
            target.heal(HEAL_AMOUNT);
            target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, REGENERATION_TICKS, 0));
            level.sendParticles(
                ParticleTypes.HEART,
                target.getX(), target.getY() + target.getBbHeight() * 0.7D, target.getZ(),
                8, 0.4D, 0.4D, 0.4D, 0.02D
            );
        }

        level.playSound(
            null,
            caster.getX(),
            caster.getY(),
            caster.getZ(),
            SoundEvents.PLAYER_LEVELUP,
            SoundSource.PLAYERS,
            0.6F,
            1.2F
        );
    }
}
