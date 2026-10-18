package com.aura.lamplight.picturesque.client.raytrace;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class VoxelTracer {
    public static final float SUN_DISTANCE = 26.0f;
    public static final int SUN_TAPS = 3;
    public static final float SKY_DISTANCE = 10.0f;
    public static final int SKY_TAPS = 4;

    private VoxelTracer() {
    }

    public static float traceSunVisibility(BlockGetter level, BlockPos pos, float[] sunDir, int taps) {
        float ox = pos.getX() + 0.5f + sunDir[0] * 0.6f;
        float oy = pos.getY() + 0.5f + sunDir[1] * 0.6f;
        float oz = pos.getZ() + 0.5f + sunDir[2] * 0.6f;
        BlockPos.MutableBlockPos scratch = new BlockPos.MutableBlockPos();
        float central = march(level, scratch, ox, oy, oz, sunDir, SUN_DISTANCE, false);
        int n = Math.max(1, taps);
        if (central >= 0.999f || n <= 1) {
            return central;
        }
        float ux;
        float uy;
        float uz;
        if (Math.abs(sunDir[1]) > 0.9f) {
            ux = 1.0f;
            uy = 0.0f;
            uz = 0.0f;
        } else {
            float inv = 1.0f / (float) Math.sqrt(sunDir[0] * sunDir[0] + sunDir[2] * sunDir[2] + 1e-6f);
            ux = -sunDir[2] * inv;
            uy = 0.0f;
            uz = sunDir[0] * inv;
        }
        float vx = sunDir[1] * uz - sunDir[2] * uy;
        float vy = sunDir[2] * ux - sunDir[0] * uz;
        float vz = sunDir[0] * uy - sunDir[1] * ux;

        float sum = 0.0f;
        for (int tap = 0; tap < n; tap++) {
            float golden = tap * 2.399963f + hash01(pos.getX(), pos.getY(), pos.getZ()) * 6.2831855f;
            float jr = (tap + 1.0f) / (n + 1.0f);
            float jx = (float) Math.cos(golden) * jr;
            float jy = (float) Math.sin(golden) * jr;
            sum += march(level, scratch,
                    ox + (ux * jx + vx * jy) * 0.35f,
                    oy + (uy * jx + vy * jy) * 0.35f,
                    oz + (uz * jx + vz * jy) * 0.35f,
                    sunDir, SUN_DISTANCE, true);
        }
        return sum / n;
    }

    public static float traceSkyVisibility(BlockGetter level, BlockPos pos) {
        float ox = pos.getX() + 0.5f;
        float oy = pos.getY() + 0.5f + 0.6f;
        float oz = pos.getZ() + 0.5f;
        BlockPos.MutableBlockPos scratch = new BlockPos.MutableBlockPos();
        float[] up = new float[]{0.0f, 1.0f, 0.0f};
        float central = march(level, scratch, ox, oy, oz, up, SKY_DISTANCE, false);
        if (central >= 0.999f) {
            return central;
        }
        float sum = 0.0f;
        for (int tap = 0; tap < SKY_TAPS; tap++) {
            float golden = tap * 2.399963f + hash01(pos.getX() ^ 0x5f3d, pos.getY(), pos.getZ() ^ 0x2b7a) * 6.2831855f;
            float r = 0.55f * ((tap + 1.0f) / (SKY_TAPS + 1.0f));
            float dx = (float) Math.cos(golden) * r;
            float dz = (float) Math.sin(golden) * r;
            float inv = 1.0f / (float) Math.sqrt(dx * dx + 1.0f + dz * dz);
            float[] dir = new float[]{dx * inv, inv, dz * inv};
            sum += march(level, scratch, ox, oy, oz, dir, SKY_DISTANCE, false);
        }
        return sum / SKY_TAPS;
    }

    private static float march(BlockGetter level, BlockPos.MutableBlockPos scratch,
            float ox, float oy, float oz, float[] dir, float distance, boolean penumbra) {
        float transmit = 1.0f;
        float step = 1.0f;
        int minY = level.getMinY();
        int maxY = level.getMaxY();
        for (float d = step; d <= distance; d += step) {
            float spread = penumbra ? 0.10f + d * 0.055f : 0.0f;
            float px = ox + dir[0] * d;
            float py = oy + dir[1] * d;
            float pz = oz + dir[2] * d;
            int bx = (int) Math.floor(px);
            int by = (int) Math.floor(py);
            int bz = (int) Math.floor(pz);
            if (by >= maxY) {
                return transmit;
            }
            if (by < minY) {
                return transmit * 0.25f;
            }
            float opacity = opacityAt(level, scratch, bx, by, bz);
            if (opacity > 0.001f) {
                float weight = penumbra ? opacity / (1.0f + spread * spread * 2.2f) : opacity;
                transmit *= 1.0f - (weight > 1.0f ? 1.0f : weight);
                if (transmit < 0.02f) {
                    return 0.0f;
                }
                if (opacity >= 0.99f && spread < 0.35f) {
                    return transmit;
                }
            }
        }
        return transmit;
    }

    private static float opacityAt(BlockGetter level, BlockPos.MutableBlockPos scratch, int x, int y, int z) {
        BlockState state;
        try {
            state = level.getBlockState(scratch.set(x, y, z));
        } catch (Exception e) {
            return 0.0f;
        }
        if (state.isAir()) {
            return 0.0f;
        }
        if (state.getBlock() instanceof LeavesBlock) {
            return 0.45f;
        }
        if (state.isSolidRender()) {
            return 1.0f;
        }
        if (state.canOcclude()) {
            return 0.85f;
        }
        return 0.0f;
    }

    private static float hash01(int x, int y, int z) {
        int h = x * 374761393 + y * 668265263 + z * 2147483647;
        h = (h ^ (h >> 13)) * 1274126177;
        return ((h ^ (h >> 16)) >>> 0) / 4294967295.0f;
    }
}
