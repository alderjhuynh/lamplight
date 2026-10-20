package com.aura.lamplight.menagerie.crow.ai;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings("all")
public class FlightMoveControl extends MoveControl {
    private final Mob parent;
    private final float speedGeneral;
    private final boolean lookAtTarget;

    public FlightMoveControl(Mob mob, float speedGeneral, boolean lookAtTarget) {
        super(mob);
        this.parent = mob;
        this.speedGeneral = speedGeneral;
        this.lookAtTarget = lookAtTarget;
    }

    @Override
    public void tick() {
        if (this.operation == Operation.MOVE_TO) {
            Vec3 delta = new Vec3(this.wantedX - parent.getX(), this.wantedY - parent.getY(), this.wantedZ - parent.getZ());
            double dist = delta.length();
            if (dist < parent.getBoundingBox().getSize()) {
                this.operation = Operation.WAIT;
                parent.setDeltaMovement(parent.getDeltaMovement().scale(0.5D));
            } else {
                parent.setDeltaMovement(parent.getDeltaMovement().add(delta.scale(this.speedModifier * speedGeneral * 0.05D / dist)));
                if (parent.getTarget() == null || !lookAtTarget) {
                    Vec3 motion = parent.getDeltaMovement();
                    parent.setYRot(-((float) Mth.atan2(motion.x, motion.z)) * Mth.RAD_TO_DEG);
                    parent.yBodyRot = parent.getYRot();
                } else {
                    double dx = parent.getTarget().getX() - parent.getX();
                    double dz = parent.getTarget().getZ() - parent.getZ();
                    parent.setYRot(-((float) Mth.atan2(dx, dz)) * Mth.RAD_TO_DEG);
                    parent.yBodyRot = parent.getYRot();
                }
            }
        } else if (this.operation == Operation.STRAFE) {
            this.operation = Operation.WAIT;
        }
    }
}
