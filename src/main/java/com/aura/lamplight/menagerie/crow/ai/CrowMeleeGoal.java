package com.aura.lamplight.menagerie.crow.ai;

import com.aura.lamplight.menagerie.crow.CrowBlockPos;
import com.aura.lamplight.menagerie.crow.CrowEntity;
import com.aura.lamplight.menagerie.crow.CrowMaths;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings("all")
public class CrowMeleeGoal extends Goal {
    private final CrowEntity crow;
    private float circlingTime;
    private float circleDistance = 1;
    private float yLevel = 2;
    private boolean clockwise;
    private int maxCircleTime;

    public CrowMeleeGoal(CrowEntity crow) {
        this.crow = crow;
    }

    @Override
    public boolean canUse() {
        return crow.getTarget() != null && !crow.isSitting() && crow.getCommand() != 3;
    }

    @Override
    public void start() {
        clockwise = crow.getRandom().nextBoolean();
        yLevel = crow.getRandom().nextInt(2);
        circlingTime = 0;
        maxCircleTime = 20 + crow.getRandom().nextInt(100);
        circleDistance = 1F + crow.getRandom().nextFloat() * 3F;
    }

    @Override
    public void stop() {
        clockwise = crow.getRandom().nextBoolean();
        yLevel = crow.getRandom().nextInt(2);
        circlingTime = 0;
        maxCircleTime = 20 + crow.getRandom().nextInt(100);
        circleDistance = 1F + crow.getRandom().nextFloat() * 3F;
        if (crow.onGround()) {
            crow.setFlying(false);
        }
    }

    @Override
    public void tick() {
        LivingEntity target = crow.getTarget();
        if (target != null) {
            if (circlingTime > maxCircleTime) {
                crow.getMoveControl().setWantedPosition(target.getX(), target.getY() + target.getEyeHeight() / 2F, target.getZ(), 1.3F);
                if (crow.distanceTo(target) < 2 && !crow.level().isClientSide()) {
                    crow.peck();
                    boolean undead = target.getType().builtInRegistryHolder().is(EntityTypeTags.UNDEAD);
                    target.hurt(target.damageSources().generic(), undead ? 4 : 1);
                    stop();
                }
            } else {
                Vec3 circle = getCirclePos(target.position());
                if (circle == null) {
                    circle = target.position();
                }
                crow.setFlying(true);
                crow.getMoveControl().setWantedPosition(circle.x(), circle.y() + target.getEyeHeight() + 0.2F, circle.z(), 1F);
            }
        }
        if (this.crow.isFlying()) {
            circlingTime++;
        }
    }

    public Vec3 getCirclePos(Vec3 target) {
        float angle = CrowMaths.EIGHT_STARTING_ANGLE * (clockwise ? -circlingTime : circlingTime);
        double extraX = circleDistance * Mth.sin(angle);
        double extraZ = circleDistance * Mth.cos(angle);
        Vec3 pos = new Vec3(target.x() + extraX, target.y() + yLevel, target.z() + extraZ);
        if (crow.level().isEmptyBlock(CrowBlockPos.fromVec3(pos))) {
            return pos;
        }
        return null;
    }
}
