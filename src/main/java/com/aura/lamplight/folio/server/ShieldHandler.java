package com.aura.lamplight.folio.server;

import com.aura.lamplight.folio.entity.FireballEntity;
import com.aura.lamplight.folio.particle.ShieldParticleOptions;
import com.aura.lamplight.folio.sound.FolioSounds;
import com.aura.lamplight.folio.spell.CastOrigin;
import com.aura.lamplight.folio.spell.ShieldGeometry;
import com.aura.lamplight.folio.spell.ShieldSpell;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ShieldHandler {
    private ShieldHandler() {}

    private static final Map<UUID, Ward> WARDS = new HashMap<>();

    private static final int HEARTBEAT_TICKS = 4;
    private static final double SWEEP_MARGIN = 10.0D;

    public static void initialize() {
        ServerTickEvents.START_SERVER_TICK.register(ShieldHandler::tick);
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> end(handler.getPlayer()));
    }

    public static void begin(final ServerLevel level, final Player caster, final CastOrigin origin) {
        WARDS.put(caster.getUUID(), new Ward(origin, level.dimension()));

        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
            FolioSounds.SHIELD_CAST, SoundSource.PLAYERS, 1.0F, 1.0F);
        sendParticle(level, caster, ShieldParticleOptions.REFRESH);
    }

    public static void end(final Player caster) {
        Ward ward = WARDS.remove(caster.getUUID());
        if (ward != null && caster.level() instanceof ServerLevel level && !caster.isRemoved()) {
            sendParticle(level, caster, ShieldParticleOptions.COLLAPSE);
        }
    }

    public static boolean isBroken(final Player caster) {
        Ward ward = WARDS.get(caster.getUUID());
        return ward != null && ward.broken;
    }

    private static void tick(final MinecraftServer server) {
        if (WARDS.isEmpty()) {
            return;
        }

        Iterator<Map.Entry<UUID, Ward>> iterator = WARDS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Ward> entry = iterator.next();
            Ward ward = entry.getValue();
            ServerPlayer caster = server.getPlayerList().getPlayer(entry.getKey());

            if (caster == null
                || !caster.isAlive()
                || caster.isRemoved()
                || caster.level().dimension() != ward.dimension) {
                iterator.remove();
                continue;
            }

            ServerLevel level = caster.level();
            if (++ward.age % HEARTBEAT_TICKS == 0) {
                sendParticle(level, caster, ShieldParticleOptions.REFRESH);
            }
            if (ward.broken) {
                continue;
            }
            sweep(level, caster, ward);
        }
    }

    private static void sweep(final ServerLevel level, final ServerPlayer caster, final Ward ward) {
        Vec3 normal = ShieldGeometry.normal(caster.getYRot(), caster.getXRot());
        Vec3 center = ShieldGeometry.center(caster.getEyePosition(), normal);
        AABB area = new AABB(center, center).inflate(ShieldGeometry.RADIUS + SWEEP_MARGIN);

        List<Projectile> projectiles = level.getEntitiesOfClass(Projectile.class, area, p -> canBlock(p, caster));
        for (Projectile projectile : projectiles) {
            Vec3 from = new Vec3(projectile.xo, projectile.yo, projectile.zo);
            Vec3 to = projectile.position();
            if (from.distanceToSqr(to) < 1.0E-6D) {
                continue;
            }
            Vec3 hit = ShieldGeometry.intersect(center, normal, from, to);
            if (hit != null) {
                deflect(level, caster, ward, projectile, hit);
                if (ward.broken) {
                    return;
                }
            }
        }
    }

    public static boolean tryBlock(final Projectile projectile) {
        if (WARDS.isEmpty() || !(projectile.level() instanceof ServerLevel level)) {
            return false;
        }

        Vec3 from = new Vec3(projectile.xo, projectile.yo, projectile.zo);
        Vec3 to = projectile.position();

        for (Map.Entry<UUID, Ward> entry : WARDS.entrySet()) {
            Ward ward = entry.getValue();
            if (ward.broken || ward.dimension != level.dimension()) {
                continue;
            }
            if (!(level.getPlayerByUUID(entry.getKey()) instanceof ServerPlayer caster) || !canBlock(projectile, caster)) {
                continue;
            }

            Vec3 normal = ShieldGeometry.normal(caster.getYRot(), caster.getXRot());
            Vec3 center = ShieldGeometry.center(caster.getEyePosition(), normal);
            Vec3 hit = ShieldGeometry.intersect(center, normal, from, to);
            if (hit != null) {
                deflect(level, caster, ward, projectile, hit);
                return true;
            }
        }
        return false;
    }

    private static boolean canBlock(final Projectile projectile, final Player caster) {
        return !projectile.isRemoved()
            && projectile.getOwner() != caster
            && !(projectile instanceof FishingHook);
    }

    private static void deflect(
        final ServerLevel level,
        final ServerPlayer caster,
        final Ward ward,
        final Projectile projectile,
        final Vec3 hit
    ) {
        if (ward.origin == CastOrigin.SPELLBOOK) {
            double cost = ShieldSpell.MANA_PER_PROJECTILE + estimateDamage(projectile) * ShieldSpell.MANA_PER_DAMAGE;
            if (!FolioManaHandler.spend(caster, cost)) {
                FolioManaHandler.spend(caster, FolioManaHandler.mana(caster));
                ward.broken = true;
                level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                    SoundEvents.SHIELD_BREAK.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }

        level.playSound(null, hit.x, hit.y, hit.z, FolioSounds.SHIELD_HIT, SoundSource.PLAYERS,
            1.0F, 0.9F + level.getRandom().nextFloat() * 0.2F);
        sendParticle(level, caster, ShieldParticleOptions.IMPACT);
        level.sendParticles(ParticleTypes.END_ROD, hit.x, hit.y, hit.z, 6, 0.12D, 0.12D, 0.12D, 0.03D);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, hit.x, hit.y, hit.z, 5, 0.1D, 0.1D, 0.1D, 0.05D);

        if (projectile instanceof FireballEntity fireball) {
            fireball.fizzle();
        } else {
            projectile.discard();
        }
    }

    private static double estimateDamage(final Projectile projectile) {
        if (projectile instanceof FireballEntity fireball) {
            return fireball.estimatedDamage();
        }
        if (projectile instanceof ThrownTrident) {
            return 8.0D;
        }
        if (projectile instanceof AbstractArrow) {
            return Math.ceil(projectile.getDeltaMovement().length() * 2.0D);
        }
        if (projectile instanceof AbstractHurtingProjectile) {
            return 6.0D;
        }
        return 2.0D;
    }

    private static void sendParticle(final ServerLevel level, final Player caster, final int mode) {
        Vec3 center = ShieldGeometry.center(caster, 1.0F);
        level.sendParticles(new ShieldParticleOptions(caster.getId(), mode),
            true, false, center.x, center.y, center.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    private static final class Ward {
        final CastOrigin origin;
        final ResourceKey<Level> dimension;
        int age;
        boolean broken;

        Ward(final CastOrigin origin, final ResourceKey<Level> dimension) {
            this.origin = origin;
            this.dimension = dimension;
        }
    }
}
