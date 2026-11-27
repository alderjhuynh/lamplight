package com.aura.lamplight.folio.spell;

import com.aura.lamplight.folio.Folio;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class TeleportSpell extends MultiCastSpell {

    public static final Identifier ID = Folio.id("teleport");

    private static final double RANGE = 8.0D;
    private static final int CASTS = 3;
    private static final int CAST_WINDOW_TICKS = 60;

    public TeleportSpell() {
        super(
            ID,
            Component.translatable("spell.lamplight.teleport"),
            SpellSchool.ARCANE,
            12,
            200,
            false,
            CASTS,
            CAST_WINDOW_TICKS
        );
    }

    @Override
    public void castAt(
        final ServerLevel level,
        final Player caster,
        final ItemStack sourceItem,
        final int castIndex
    ) {
        Vec3 from = caster.getEyePosition();
        Vec3 look = caster.getLookAngle();
        Vec3 to = from.add(look.scale(RANGE));

        HitResult hit = level.clip(new ClipContext(
            from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster
        ));
        if (hit.getType() != HitResult.Type.MISS) {
            to = hit.getLocation().subtract(look.scale(0.5D));
        }

        double startX = caster.getX();
        double startY = caster.getY();
        double startZ = caster.getZ();

        if (!caster.randomTeleport(to.x, to.y, to.z, false)) {
            level.playSound(null, startX, startY, startZ,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.4F, 0.6F);
            return;
        }

        double endY = caster.getY() + caster.getBbHeight() * 0.5D;
        level.playSound(null, startX, startY, startZ,
            SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8F, 1.4F);
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
            SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8F, 0.8F);

        level.sendParticles(ParticleTypes.PORTAL,
            startX, startY + caster.getBbHeight() * 0.5D, startZ,
            24, 0.4D, 0.6D, 0.4D, 0.1D);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL,
            caster.getX(), endY, caster.getZ(),
            24, 0.4D, 0.6D, 0.4D, 0.1D);
    }
}
