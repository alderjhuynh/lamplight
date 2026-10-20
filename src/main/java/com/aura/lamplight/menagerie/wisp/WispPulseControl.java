package com.aura.lamplight.menagerie.wisp;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * The sky-jellyfish engine. Instead of walking, the wisp stores a target and
 * converts the 40-tick pulse cycle into a sharp squeeze of thrust followed by
 * a long, dragging bloom. The same phase drives the shader, so the animation
 * and the motion stay in step.
 */
public class WispPulseControl extends MoveControl<WispEntity> {
    private boolean pulseSounded;

    public WispPulseControl(WispEntity wisp) {
        super(wisp);
    }

    @Override
    public void tick() {
        WispEntity wisp = this.mob;
        long gameTime = wisp.level().getGameTime();
        float phase = (float) (gameTime % WispEntity.PULSE_CYCLE) / WispEntity.PULSE_CYCLE;

        if (phase < 0.5F) {
            this.pulseSounded = false;
        }
        if (!this.pulseSounded && phase >= 0.58F) {
            this.pulseSounded = true;
            if (this.operation == Operation.MOVE_TO && !wisp.isSilent()) {
                wisp.playPulseSound();
            }
        }

        if (wisp.isOrderedToSit()) {
            wisp.setDeltaMovement(wisp.getDeltaMovement().scale(0.75D));
            return;
        }

        if (this.operation == Operation.MOVE_TO) {
            Vec3 offset = new Vec3(this.wantedX - wisp.getX(), this.wantedY - wisp.getY(), this.wantedZ - wisp.getZ());
            double distance = offset.length();

            if (distance < 0.55D) {
                this.operation = Operation.WAIT;
                wisp.setDeltaMovement(wisp.getDeltaMovement().scale(0.82D));
                return;
            }

            Vec3 direction = offset.scale(1.0D / distance);
            double maxSpeed = Math.max(0.02D, this.speedModifier);
            double thrust = pulseThrust(phase);
            Vec3 velocity = wisp.getDeltaMovement().scale(0.90D);

            velocity = velocity.add(direction.scale(maxSpeed * (0.0035D + 0.16D * thrust)));
            velocity = clampHorizontal(velocity, maxSpeed);

            double desiredY = Mth.clamp(offset.y * 0.15D, -0.12D, 0.12D);
            velocity = new Vec3(velocity.x, velocity.y + (desiredY - velocity.y) * 0.25D, velocity.z);
            wisp.setDeltaMovement(velocity);

            float yaw = (float) (Mth.atan2(-direction.x, direction.z) * Mth.RAD_TO_DEG);
            float bodyYaw = this.rotlerp(wisp.getYRot(), yaw, 12.0F);
            wisp.setYRot(bodyYaw);
            wisp.yBodyRot = bodyYaw;
            wisp.yHeadRot = bodyYaw;
            return;
        }

        // Idle hover. Keep a little momentum and a slow, weightless bob, and
        // drift up to the wisp's resting height above the floor.
        Vec3 velocity = wisp.getDeltaMovement();
        double bob = Math.sin((gameTime + wisp.getId()) * 0.055D) * 0.0025D;
        wisp.setDeltaMovement(new Vec3(
                velocity.x * 0.94D,
                velocity.y * 0.88D + bob + idleLift(),
                velocity.z * 0.94D));
    }

    private double idleLift() {
        double hoverY = this.findHoverY();
        double gap = hoverY - this.mob.getY();
        if (gap < 0.2D) {
            return 0.0D;
        }
        double step = Math.min(gap, 0.3D);
        if (!this.mob.level().noCollision(this.mob, this.mob.getBoundingBox().move(0.0D, step, 0.0D))) {
            return 0.0D;
        }
        return Mth.clamp(gap * 0.03D, 0.0D, 0.025D);
    }

    private double findHoverY() {
        BlockPos.MutableBlockPos pos = this.mob.blockPosition().mutable();
        for (int distance = 0; distance < 5; distance++) {
            BlockState state = this.mob.level().getBlockState(pos);
            if (!state.getCollisionShape(this.mob.level(), pos).isEmpty()) {
                return pos.getY() + 2.5D;
            }
            pos.move(0, -1, 0);
        }
        return this.mob.getY();
    }

    private static Vec3 clampHorizontal(Vec3 velocity, double maxSpeed) {
        double horizontal = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
        if (horizontal <= maxSpeed) {
            return velocity;
        }
        double scale = maxSpeed / horizontal;
        return new Vec3(velocity.x * scale, velocity.y, velocity.z * scale);
    }

    private static double pulseThrust(float phase) {
        double offset = (phase - 0.60D) / 0.065D;
        return Math.exp(-offset * offset);
    }
}
