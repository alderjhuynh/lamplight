package com.aura.lamplight.menagerie.raccoon.ai;

import com.aura.lamplight.menagerie.raccoon.RaccoonEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class RaccoonTargetItemsGoal extends TargetGoal {
    private static final int DEFAULT_EXECUTION_CHANCE = 10;
    private static final int DEFAULT_TICK_THRESHOLD = 0;
    private static final double FOLLOW_DISTANCE = 16.0D;

    private final RaccoonEntity raccoon;
    private final Sorter sorter;
    private final int executionChance;
    private boolean mustUpdate;
    @Nullable
    private ItemEntity targetEntity;
    private final int tickThreshold;
    private float radius = 9.0F;
    private int walkCooldown = 0;

    public RaccoonTargetItemsGoal(RaccoonEntity raccoon, boolean checkSight) {
        this(raccoon, DEFAULT_EXECUTION_CHANCE, checkSight, false, DEFAULT_TICK_THRESHOLD, 9.0F);
    }

    public RaccoonTargetItemsGoal(RaccoonEntity raccoon, int executionChance, boolean checkSight, boolean onlyNearby,
                                  int tickThreshold, float radius) {
        super(raccoon, checkSight, onlyNearby);
        this.executionChance = executionChance;
        this.tickThreshold = tickThreshold;
        this.raccoon = raccoon;
        this.sorter = new Sorter(raccoon);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    @Override
    public boolean canUse() {
        if (this.mob.isPassenger() || this.mob.isVehicle() && this.mob.getControllingPassenger() != null) {
            return false;
        }
        if (!this.mob.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
            return false;
        }
        if (!this.mustUpdate) {
            long worldTime = this.mob.level().getGameTime() % 10;
            if (this.mob.getNoActionTime() >= 100 && worldTime != 0) {
                return false;
            }
            if (this.mob.getRandom().nextInt(this.executionChance) != 0 && worldTime != 0) {
                return false;
            }
        }
        List<ItemEntity> list = this.mob.level().getEntitiesOfClass(ItemEntity.class,
                this.getTargetableArea(FOLLOW_DISTANCE), this::isValidItemTarget);
        if (list.isEmpty()) {
            return false;
        }
        Collections.sort(list, this.sorter);
        this.targetEntity = list.get(0);
        this.mustUpdate = false;
        return true;
    }

    private boolean isValidItemTarget(ItemEntity item) {
        ItemStack stack = item.getItem();
        return !stack.isEmpty() && this.raccoon.canTargetItem(stack) && item.tickCount > this.tickThreshold;
    }

    private AABB getTargetableArea(double targetDistance) {
        Vec3 center = new Vec3(this.mob.getX() + 0.5, this.mob.getY() + 0.5, this.mob.getZ() + 0.5);
        AABB aabb = new AABB(-radius, -radius, -radius, radius, radius, radius);
        return aabb.move(center);
    }

    @Override
    public void start() {
        this.moveToItem();
        super.start();
    }

    private void moveTowardItem() {
        if (this.walkCooldown > 0) {
            this.walkCooldown--;
        } else {
            this.mob.getNavigation().moveTo(this.targetEntity.getX(), this.targetEntity.getY(), this.targetEntity.getZ(), 1.0F);
            this.walkCooldown = 30 + this.mob.getRandom().nextInt(40);
        }
    }

    private void moveToItem() {
        this.moveTowardItem();
    }

    @Override
    public void stop() {
        super.stop();
        this.mob.getNavigation().stop();
        this.targetEntity = null;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.targetEntity == null || !this.targetEntity.isAlive()) {
            this.stop();
            this.mob.getNavigation().stop();
        } else {
            this.moveTowardItem();
        }
        if (this.targetEntity != null && this.targetEntity.isAlive()
                && this.mob.distanceToSqr(this.targetEntity) < this.raccoon.getMaxDistToItem()
                && this.mob.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
            this.raccoon.onGetItem(this.targetEntity);
            this.targetEntity.getItem().shrink(1);
            this.stop();
        }
    }

    public void makeUpdate() {
        this.mustUpdate = true;
    }

    @Override
    public boolean canContinueToUse() {
        boolean path = !this.mob.getNavigation().isDone();
        return path && this.targetEntity != null && this.targetEntity.isAlive();
    }

    private record Sorter(com.aura.lamplight.menagerie.raccoon.RaccoonEntity theEntity) implements Comparator<net.minecraft.world.entity.Entity> {
        @Override
        public int compare(net.minecraft.world.entity.Entity a, net.minecraft.world.entity.Entity b) {
            return Double.compare(this.theEntity.distanceToSqr(a), this.theEntity.distanceToSqr(b));
        }
    }
}
