package com.aura.lamplight.menagerie.wisp.ai;

import com.aura.lamplight.menagerie.registry.MenagerieTags;
import com.aura.lamplight.menagerie.wisp.WispEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class WispStartleGoal extends Goal {
    private final WispEntity wisp;
    private Entity threat;
    private int updateTimer;

    public WispStartleGoal(WispEntity wisp) {
        this.wisp = wisp;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.wisp.isTame() || this.wisp.isOrderedToSit()) {
            return false;
        }
        if (this.wisp.getStartleTicks() > 0) {
            return true;
        }
        if (this.wisp.getRandom().nextInt(10) != 0) {
            return false;
        }

        Entity scarier = this.findScatterer();
        if (scarier == null) {
            return false;
        }
        this.threat = scarier;
        this.wisp.startle();
        return true;
    }

    private Entity findScatterer() {
        List<Entity> entities = this.wisp.level().getEntitiesOfClass(
                Entity.class,
                this.wisp.getBoundingBox().inflate(7.0D),
                entity -> entity.isAlive()
                        && entity.getType().builtInRegistryHolder().is(MenagerieTags.SCATTERS_WISPS));
        if (entities.isEmpty()) {
            return null;
        }
        entities.sort(Comparator.comparingDouble(this.wisp::distanceToSqr));
        return entities.getFirst();
    }

    @Override
    public boolean canContinueToUse() {
        return !this.wisp.isTame() && this.wisp.getStartleTicks() > 0;
    }

    @Override
    public void start() {
        this.updateTimer = 0;
    }

    @Override
    public void tick() {
        if (--this.updateTimer > 0) {
            return;
        }
        this.updateTimer = 8;

        LivingEntity hurtBy = this.wisp.getLastHurtByMob();
        if (hurtBy != null && hurtBy.isAlive()) {
            this.threat = hurtBy;
        } else if (this.threat == null || !this.threat.isAlive()) {
            this.threat = this.findScatterer();
        }

        Vec3 away;
        if (this.threat != null) {
            away = this.wisp.position().subtract(this.threat.position());
        } else {
            away = new Vec3(
                    this.wisp.getRandom().nextDouble() - 0.5D,
                    0.0D,
                    this.wisp.getRandom().nextDouble() - 0.5D);
        }

        Vec3 horizontal = new Vec3(away.x, 0.0D, away.z);
        if (horizontal.lengthSqr() < 1.0E-4D) {
            horizontal = new Vec3(1.0D, 0.0D, 0.0D);
        }
        horizontal = horizontal.normalize().scale(6.0D + this.wisp.getRandom().nextDouble() * 3.0D);

        double targetY = this.wisp.getY() + 1.5D + this.wisp.getRandom().nextDouble() * 2.5D;
        this.wisp.getMoveControl().setWantedPosition(
                this.wisp.getX() + horizontal.x,
                targetY,
                this.wisp.getZ() + horizontal.z,
                0.26D);
    }

    @Override
    public void stop() {
        this.threat = null;
    }
}
