package com.aura.lamplight.menagerie.crow;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public final class CrowBlockPos {
    private CrowBlockPos() {
    }

    public static BlockPos fromCoords(double x, double y, double z) {
        return new BlockPos((int) x, (int) y, (int) z);
    }

    public static BlockPos fromVec3(Vec3 vec) {
        return fromCoords(vec.x, vec.y, vec.z);
    }
}
