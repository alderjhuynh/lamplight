package com.aura.lamplight.folio.spell;

import com.aura.lamplight.folio.Folio;
import com.aura.lamplight.folio.entity.FireballEntity;
import com.aura.lamplight.folio.server.FolioManaHandler;
import com.aura.lamplight.folio.sound.FolioSounds;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class FireballSpell extends ChargeSpell {

    public static final Identifier ID = Folio.id("fireball");

    private static final int FULL_CHARGE_HOLD_TICKS = 100;
    private static final double CHARGE_MANA_PER_TICK = 0.4D;

    private static final Map<UUID, FireballEntity> ORBS = new HashMap<>();

    public FireballSpell() {
        super(ID, Component.translatable("spell.lamplight.fireball"), SpellSchool.FLAME, 10, 40, false);
    }

    @Override
    public int maxChargeTicks() {
        return FireballEntity.MAX_CHARGE_TICKS;
    }

    @Override
    public int maxHoldTicks() {
        return FireballEntity.MAX_CHARGE_TICKS + FULL_CHARGE_HOLD_TICKS;
    }

    @Override
    public float chargeProgress(final Player caster, final int heldTicks) {
        FireballEntity orb = ORBS.get(caster.getUUID());
        return orb == null ? 0.0F : orb.charge();
    }

    public static boolean isOrbOf(final Entity owner, final FireballEntity orb) {
        return ORBS.get(owner.getUUID()) == orb;
    }

    @Override
    public void chargeStart(final ServerLevel level, final Player caster, final ItemStack source, final CastOrigin origin) {
        FireballEntity stale = ORBS.remove(caster.getUUID());
        if (stale != null) {
            stale.discard();
        }

        FireballEntity orb = new FireballEntity(level, caster);
        ORBS.put(caster.getUUID(), orb);
        level.addFreshEntity(orb);

        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
            FolioSounds.FIREBALL_PREPARE, SoundSource.PLAYERS, 0.8F, 1.0F);
    }

    @Override
    public boolean chargeTick(
        final ServerLevel level,
        final Player caster,
        final ItemStack source,
        final CastOrigin origin,
        final int heldTicks
    ) {
        FireballEntity orb = ORBS.get(caster.getUUID());
        if (orb == null || orb.isRemoved()) {
            ORBS.remove(caster.getUUID());
            return false;
        }

        if (!orb.isFullyCharged() && !orb.isStarved()) {
            boolean paid = origin == CastOrigin.SCROLL || FolioManaHandler.spend(caster, CHARGE_MANA_PER_TICK);
            if (paid) {
                orb.addChargeTick();
                playChargeCue(level, orb);
            } else {
                orb.setStarved(true);
                if (caster instanceof ServerPlayer player) {
                    player.sendSystemMessage(
                        Component.translatable("message.lamplight.spellbook.no_mana").withStyle(ChatFormatting.RED), true);
                }
                level.playSound(null, orb.getX(), orb.getY(), orb.getZ(),
                    SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.5F, 1.4F);
            }
        }

        if (heldTicks % 6 == 0) {
            level.playSound(null, orb.getX(), orb.getY(), orb.getZ(), SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS,
                0.3F + orb.charge() * 0.7F, 0.7F + orb.charge() * 0.7F);
        }
        return true;
    }

    private static void playChargeCue(final ServerLevel level, final FireballEntity orb) {
        int ticks = orb.chargeTicks();
        int third = FireballEntity.MAX_CHARGE_TICKS / 3;
        if (ticks % third != 0) {
            return;
        }
        float pitch = 0.7F + 0.2F * (ticks / third);
        level.playSound(null, orb.getX(), orb.getY(), orb.getZ(), FolioSounds.FIREBALL_INITIAL_CAST, SoundSource.PLAYERS, 0.8F, pitch);
        if (orb.isFullyCharged()) {
            level.playSound(null, orb.getX(), orb.getY(), orb.getZ(), SoundEvents.LAVA_POP, SoundSource.PLAYERS, 1.0F, 0.6F);
        }
    }

    @Override
    public void chargeRelease(
        final ServerLevel level,
        final Player caster,
        final ItemStack source,
        final CastOrigin origin,
        final int heldTicks
    ) {
        FireballEntity orb = ORBS.remove(caster.getUUID());
        if (orb != null && !orb.isRemoved()) {
            orb.launch(caster);
        }
    }

    @Override
    public void chargeCancel(final ServerLevel level, final Player caster, final CastOrigin origin) {
        FireballEntity orb = ORBS.remove(caster.getUUID());
        if (orb != null && !orb.isRemoved()) {
            orb.fizzle();
        }
    }
}
