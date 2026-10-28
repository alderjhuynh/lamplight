package com.aura.lamplight.menagerie.raccoon.ai;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class RaccoonWanderRangedGoal extends RandomStrollGoal {
    protected final float probability;
    protected final int xzRange;
    protected final int yRange;

    public RaccoonWanderRangedGoal(PathfinderMob mob, int chance, double speed, int xzRange, int yRange) {
        this(mob, chance, speed, 0.001F, xzRange, yRange);
    }

    @Override
    public boolean canUse() {
        if (this.mob.isVehicle()) {
            return false;
        }
        if (!this.forceTrigger) {
            if (this.mob.getNoActionTime() >= 100) {
                return false;
            }
            if (this.mob.getRandom().nextInt(this.interval) != 0) {
                return false;
            }
        }
        Vec3 pos = this.getPosition();
        if (pos == null) {
            return false;
        }
        this.wantedX = pos.x;
        this.wantedY = pos.y;
        this.wantedZ = pos.z;
        this.forceTrigger = false;
        return true;
    }

    public RaccoonWanderRangedGoal(PathfinderMob mob, int chance, double speed, float probability, int xzRange, int yRange) {
        super(mob, speed, chance);
        this.probability = probability;
        this.xzRange = xzRange;
        this.yRange = yRange;
    }

    @Nullable
    @Override
    protected Vec3 getPosition() {
        if (this.mob.isInWaterOrRain()) {
            Vec3 pos = LandRandomPos.getPos(this.mob, this.xzRange, this.yRange);
            return pos == null ? super.getPosition() : pos;
        }
        return this.mob.getRandom().nextFloat() >= this.probability ? LandRandomPos.getPos(this.mob, this.xzRange, this.yRange) : super.getPosition();
    }
}
