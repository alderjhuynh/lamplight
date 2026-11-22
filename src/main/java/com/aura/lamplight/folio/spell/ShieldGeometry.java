package com.aura.lamplight.folio.spell;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

public final class ShieldGeometry {
    private ShieldGeometry() {}

    public static final double RADIUS = 1.35D;
    public static final double DISTANCE = 1.7D;
    public static final float MAX_PITCH = 40.0F;
    private static final double DROP = 0.55D;
    private static final double THICKNESS = 0.6D;

    public static Vec3 normal(final float yRot, final float xRot) {
        float pitch = Mth.clamp(xRot, -MAX_PITCH, MAX_PITCH) * Mth.DEG_TO_RAD;
        float yaw = yRot * Mth.DEG_TO_RAD;
        double horizontal = Mth.cos(pitch);
        return new Vec3(-Mth.sin(yaw) * horizontal, -Mth.sin(pitch), Mth.cos(yaw) * horizontal);
    }

    public static Vec3 center(final Vec3 eye, final Vec3 normal) {
        return eye.add(0.0D, -DROP, 0.0D).add(normal.scale(DISTANCE));
    }

    public static Vec3 center(final Entity owner, final float partialTick) {
        Vec3 normal = normal(owner.getViewYRot(partialTick), owner.getViewXRot(partialTick));
        return center(owner.getEyePosition(partialTick), normal);
    }

    public static Quaternionf orientation(final float yRot, final float xRot) {
        float pitch = Mth.clamp(xRot, -MAX_PITCH, MAX_PITCH) * Mth.DEG_TO_RAD;
        float yaw = yRot * Mth.DEG_TO_RAD;
        return new Quaternionf().rotationYXZ(-yaw, pitch, 0.0F);
    }

    public static @Nullable Vec3 intersect(final Vec3 center, final Vec3 normal, final Vec3 from, final Vec3 to) {
        double d0 = from.subtract(center).dot(normal);
        double d1 = to.subtract(center).dot(normal);

        Vec3 point;
        if (d0 > 0.0D && d1 <= 0.0D) {
            point = from.lerp(to, d0 / (d0 - d1));
        } else if (d1 <= 0.0D && d1 > -THICKNESS && d1 < d0) {
            point = to;
        } else {
            return null;
        }

        Vec3 offset = point.subtract(center);
        Vec3 inPlane = offset.subtract(normal.scale(offset.dot(normal)));
        return inPlane.lengthSqr() <= RADIUS * RADIUS ? point : null;
    }
}
