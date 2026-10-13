package com.aura.picturesque.client.raytrace;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public final class BounceSampler {
    public static final int GLASS_SCAN_HEIGHT = 4;

    private BounceSampler() {
    }

    public static float sampleBounce(BlockGetter level, BlockPos pos, float[] out) {
        BlockPos.MutableBlockPos scratch = new BlockPos.MutableBlockPos();
        float r = 0.0f;
        float g = 0.0f;
        float b = 0.0f;
        int count = 0;
        int[] dx = {1, -1, 0, 0, 0, 0};
        int[] dy = {0, 0, 1, -1, 0, 0};
        int[] dz = {0, 0, 0, 0, 1, -1};
        for (int i = 0; i < 6; i++) {
            int packed;
            try {
                BlockState state = level.getBlockState(
                        scratch.set(pos.getX() + dx[i], pos.getY() + dy[i], pos.getZ() + dz[i]));
                if (state.isAir()) {
                    continue;
                }
                packed = state.getMapColor(level, scratch).col;
            } catch (Exception e) {
                continue;
            }
            r += ((packed >> 16) & 0xFF) / 255.0f;
            g += ((packed >> 8) & 0xFF) / 255.0f;
            b += (packed & 0xFF) / 255.0f;
            count++;
        }
        if (count == 0) {
            out[0] = 1.0f;
            out[1] = 1.0f;
            out[2] = 1.0f;
            return 0.0f;
        }
        out[0] = r / count;
        out[1] = g / count;
        out[2] = b / count;
        return 1.0f;
    }

    public static int glassAbove(BlockGetter level, BlockPos pos) {
        BlockPos.MutableBlockPos scratch = new BlockPos.MutableBlockPos();
        for (int dy = 1; dy <= GLASS_SCAN_HEIGHT; dy++) {
            BlockState state;
            try {
                state = level.getBlockState(scratch.set(pos.getX(), pos.getY() + dy, pos.getZ()));
            } catch (Exception e) {
                return -1;
            }
            if (state.isAir()) {
                continue;
            }
            if (state.getBlock() instanceof StainedGlassBlock
                    || state.getBlock() instanceof StainedGlassPaneBlock) {
                try {
                    return state.getMapColor(level, scratch).col;
                } catch (Exception e) {
                    return -1;
                }
            }
            return -1;
        }
        return -1;
    }

    public static boolean isWater(BlockGetter level, BlockPos pos) {
        try {
            return isWaterFluid(level.getFluidState(pos).getType());
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean waterAbove(BlockGetter level, BlockPos pos) {
        BlockPos.MutableBlockPos scratch = new BlockPos.MutableBlockPos(pos.getX(), pos.getY() + 1, pos.getZ());
        try {
            return isWaterFluid(level.getFluidState(scratch).getType());
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isWaterFluid(net.minecraft.world.level.material.Fluid type) {
        return type == Fluids.WATER || type == Fluids.FLOWING_WATER;
    }
}
