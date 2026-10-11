package com.aura.menagerie.crow.ai;

import com.aura.menagerie.crow.TargetsDroppedItems;
import com.google.common.base.Predicate;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jspecify.annotations.Nullable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

// Ported from Alex's Mobs CreatureAITargetItems (LGPL-3.0, see NOTICE_CROW.md).
public class TargetItemsGoal extends TargetGoal {
    private final Sorter sorter;
    private final Predicate<? super ItemEntity> selector;
    private final TargetsDroppedItems hunter;
    private final int tickThreshold;
    private final float radius;
    protected int executionChance = 10;
    private boolean mustUpdate;
    protected ItemEntity targetEntity;
    private int walkCooldown;

    public TargetItemsGoal(PathfinderMob mob, boolean checkSight, boolean onlyNearby, int tickThreshold, int radius) {
        super(mob, checkSight, onlyNearby);
        this.executionChance = 10;
        this.tickThreshold = tickThreshold;
        this.radius = radius;
        this.hunter = (TargetsDroppedItems) mob;
        this.sorter = new Sorter(mob);
        this.selector = new Predicate<ItemEntity>() {
            @Override
            public boolean apply(@Nullable ItemEntity item) {
                if (item == null) {
                    return false;
                }
                ItemStack stack = item.getItem();
                return !stack.isEmpty() && hunter.canTargetItem(stack) && item.tickCount > tickThreshold;
            }
        };
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.mob.isPassenger() || (mob.isVehicle() && mob.getControllingPassenger() != null)) {
            return false;
        }
        if (!mob.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
            return false;
        }
        if (!this.mustUpdate) {
            long time = this.mob.level().getGameTime() % 10;
            if (this.mob.getNoActionTime() >= 100 && time != 0) {
                return false;
            }
            if (this.mob.getRandom().nextInt(this.executionChance) != 0 && time != 0) {
                return false;
            }
        }
        List<ItemEntity> list = this.mob.level().getEntitiesOfClass(ItemEntity.class, getTargetableArea(16.0D), this.selector);
        if (list.isEmpty()) {
            return false;
        }
        Collections.sort(list, this.sorter);
        this.targetEntity = list.get(0);
        this.mustUpdate = false;
        this.hunter.onFindTarget(targetEntity);
        return true;
    }

    protected AABB getTargetableArea(double range) {
        Vec3 center = new Vec3(this.mob.getX() + 0.5, this.mob.getY() + 0.5, this.mob.getZ() + 0.5D);
        return new AABB(-radius, -radius, -radius, radius, radius, radius).move(center);
    }

    @Override
    public void start() {
        moveTo();
        super.start();
    }

    protected void moveTo() {
        if (walkCooldown > 0) {
            walkCooldown--;
        } else {
            this.mob.getNavigation().moveTo(this.targetEntity.getX(), this.targetEntity.getY(), this.targetEntity.getZ(), 1);
            walkCooldown = 30 + this.mob.getRandom().nextInt(40);
        }
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
            return;
        }
        moveTo();
        if (this.targetEntity.isAlive() && this.mob.distanceToSqr(this.targetEntity) < this.hunter.getMaxDistToItem()
                && mob.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
            hunter.onGetItem(targetEntity);
            this.targetEntity.getItem().shrink(1);
            stop();
        }
    }

    private record Sorter(PathfinderMob mob) implements java.util.Comparator<ItemEntity> {
        @Override
        public int compare(ItemEntity a, ItemEntity b) {
            return Double.compare(mob.distanceToSqr(a), mob.distanceToSqr(b));
        }
    }
}
