package com.aura.lamplight.folio.entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class BloodThrallEntity extends Zombie {

    public static final int LIFETIME_TICKS = 30 * 20;
    public static final int MAX_PER_OWNER = 3;
    private static final double FOLLOW_START_DISTANCE = 8.0D;
    private static final double TELEPORT_DISTANCE = 24.0D;
    private static final int OWNER_COMBAT_MEMORY_TICKS = 100;

    private @Nullable UUID ownerId;
    private @Nullable UUID firstVictimId;
    private int lifeTicks = LIFETIME_TICKS;

    public BloodThrallEntity(final EntityType<? extends BloodThrallEntity> type, final Level level) {
        super(type, level);
        this.xpReward = 0;
    }

     public static void summon(final ServerLevel level, final LivingEntity owner, final LivingEntity victim) {
        if (owner.level() != level) {
            return;
        }

        if (owner instanceof BloodThrallEntity) {
            return;
        }

        List<BloodThrallEntity> owned = new ArrayList<>();
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof BloodThrallEntity thrall && thrall.isAlive() && thrall.isOwnedBy(owner)) {
                owned.add(thrall);
            }
        }
        owned.sort(Comparator.comparingInt(thrall -> thrall.lifeTicks));
        for (int i = 0; i <= owned.size() - MAX_PER_OWNER; i++) {
            owned.get(i).crumble(level);
        }

        BloodThrallEntity thrall = new BloodThrallEntity(ModEntityTypes.BLOOD_THRALL, level);
        thrall.ownerId = owner.getUUID();
        thrall.firstVictimId = victim.getUUID();

        thrall.snapTo(victim.getX() + 0.8D, victim.getY(), victim.getZ(), victim.getYRot(), 0.0F);
        if (!level.noCollision(thrall)) {
            thrall.snapTo(victim.getX(), victim.getY(), victim.getZ(), victim.getYRot(), 0.0F);
        }
        if (!level.noCollision(thrall)) {
            thrall.snapTo(owner.getX(), owner.getY(), owner.getZ(), owner.getYRot(), 0.0F);
        }

        level.addFreshEntity(thrall);
        level.sendParticles(ParticleTypes.CRIMSON_SPORE, thrall.getX(), thrall.getY(0.5D), thrall.getZ(),
            40, 0.4D, 0.8D, 0.4D, 0.02D);
    }

    public boolean isOwnedBy(final Entity entity) {
        return ownerId != null && ownerId.equals(entity.getUUID());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(3, new FollowSummonerGoal());
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    protected void customServerAiStep(final ServerLevel level) {
        super.customServerAiStep(level);

        LivingEntity owner = owner(level);
        if (--lifeTicks <= 0 || owner == null || !owner.isAlive()) {
            crumble(level);
            return;
        }
        if (this.tickCount % 5 == 0) {
            chooseTarget(level, owner);
        }
    }

    private void chooseTarget(final ServerLevel level, final LivingEntity owner) {
        LivingEntity wanted = null;

        LivingEntity attacker = owner.getLastHurtByMob();
        if (attacker != null && owner.tickCount - owner.getLastHurtByMobTimestamp() <= OWNER_COMBAT_MEMORY_TICKS
            && canAttack(attacker)) {
            wanted = attacker;
        }
        if (wanted == null) {
            LivingEntity struck = owner.getLastHurtMob();
            if (struck != null && owner.tickCount - owner.getLastHurtMobTimestamp() <= OWNER_COMBAT_MEMORY_TICKS
                && canAttack(struck)) {
                wanted = struck;
            }
        }
        if (wanted == null && firstVictimId != null
            && level.getEntity(firstVictimId) instanceof LivingEntity victim && canAttack(victim)) {
            wanted = victim;
        }
        if (wanted == null && owner instanceof Mob ownerMob && ownerMob.getTarget() != null
            && canAttack(ownerMob.getTarget())) {
            wanted = ownerMob.getTarget();
        }
        this.setTarget(wanted);
    }

    @Override
    public boolean canAttack(final LivingEntity target) {
        if (!target.isAlive() || target == this || isOwnedBy(target)) {
            return false;
        }
        if (target instanceof BloodThrallEntity other && ownerId != null && ownerId.equals(other.ownerId)) {
            return false;
        }
        if (target instanceof OwnableEntity pet && pet.getOwnerReference() != null
            && pet.getOwnerReference().getUUID().equals(ownerId)) {
            return false;
        }
        if (this.level() instanceof ServerLevel level) {
            LivingEntity owner = owner(level);
            if (owner != null && owner.isAlliedTo(target)) {
                return false;
            }
            if (owner instanceof Player player && target instanceof Player victim && !player.canHarmPlayer(victim)) {
                return false;
            }
        }
        return super.canAttack(target);
    }

    @Override
    public boolean doHurtTarget(final ServerLevel level, final Entity target) {
        if (target instanceof LivingEntity living && ownerId != null && owner(level) instanceof Player) {
            living.setLastHurtByPlayer(ownerId, 100);
        }
        return super.doHurtTarget(level, target);
    }

    @Override
    public boolean hurtServer(final ServerLevel level, final DamageSource source, final float damage) {
        Entity attacker = source.getEntity();
        if (attacker != null && isOwnedBy(attacker)) {
            return false;
        }
        return super.hurtServer(level, source, damage);
    }

    private @Nullable LivingEntity owner(final ServerLevel level) {
        if (ownerId == null) {
            return null;
        }
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(ownerId);
        if (player != null) {
            return player.level() == level ? player : null;
        }
        return level.getEntity(ownerId) instanceof LivingEntity living ? living : null;
    }

    private void crumble(final ServerLevel level) {
        level.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY(0.5D), this.getZ(),
            20, 0.3D, 0.6D, 0.3D, 0.02D);
        this.discard();
    }

    @Override
    protected boolean convertsInWater() {
        return false;
    }

    @Override
    public boolean killedEntity(final ServerLevel level, final LivingEntity entity, final DamageSource source) {
        return true; // i know this is needed but idfk for what
    } // OH ZILLAGER CONVERSION DUH

    @Override
    public boolean isPreventingPlayerRest(final ServerLevel level, final Player player) {
        return !isOwnedBy(player) && super.isPreventingPlayerRest(level, player);
    }

    @Override
    public boolean removeWhenFarAway(final double distanceSqr) {
        return false;
    }

    @Override
    public boolean canUsePortal(final boolean ignorePassenger) {
        return false;
    }

    @Override
    protected boolean shouldDropLoot(final ServerLevel level) {
        return false;
    }

    @Override
    public boolean shouldDropExperience() {
        return false;
    }

    private final class FollowSummonerGoal extends Goal {
        private @Nullable LivingEntity summoner;

        private FollowSummonerGoal() {
            this.setFlags(java.util.EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (!(level() instanceof ServerLevel level) || getTarget() != null) {
                return false;
            }
            summoner = owner(level);
            return summoner != null && distanceToSqr(summoner) > FOLLOW_START_DISTANCE * FOLLOW_START_DISTANCE;
        }

        @Override
        public boolean canContinueToUse() {
            return summoner != null && summoner.isAlive() && getTarget() == null && distanceToSqr(summoner) > 9.0D;
        }

        @Override
        public void tick() {
            if (summoner == null) {
                return;
            }
            if (distanceToSqr(summoner) > TELEPORT_DISTANCE * TELEPORT_DISTANCE) {
                snapTo(summoner.getX(), summoner.getY(), summoner.getZ(), getYRot(), getXRot());
                getNavigation().stop();
            } else if (tickCount % 10 == 0 || getNavigation().isDone()) {
                getNavigation().moveTo(summoner, 1.2D);
            }
        }

        @Override
        public void stop() {
            summoner = null;
            getNavigation().stop();
        }
    }
}
