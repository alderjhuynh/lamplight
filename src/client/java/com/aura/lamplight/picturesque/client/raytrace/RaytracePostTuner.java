package com.aura.lamplight.picturesque.client.raytrace;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import com.aura.lamplight.picturesque.Picturesque;
import com.aura.lamplight.picturesque.client.PicturesqueConfig;
import com.aura.lamplight.picturesque.client.PicturesqueState;
import com.aura.lamplight.client.mixin.picturesque.raytrace.PostChainAccessor;
import com.aura.lamplight.client.mixin.picturesque.raytrace.PostPassAccessor;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;

public final class RaytracePostTuner {
    private static final String UNIFORM_BLOCK = "RayConfig";
    private static final int FLOAT_COUNT = 3;
    private static final float BLOOM_GAIN = 0.7f;
    private static final int RETIRE_AFTER_TICKS = 10;
    private static final int SYNC_EVERY_TICKS = 20;

    private static int tickCounter;
    private static float lastStrength = Float.NaN;
    private static float lastVibrant = Float.NaN;
    private static boolean warned;
    private static final Deque<RetiredBuffer> RETIRED = new ArrayDeque<>();

    private record RetiredBuffer(GpuBuffer buffer, int tick) {
    }

    private RaytracePostTuner() {
    }

    public static void tick(Minecraft client) {
        tickCounter++;
        drain();
        RaytraceState rt = RaytraceState.get();
        if (!rt.active() || client.level == null || client.gameRenderer == null) {
            return;
        }
        PicturesqueConfig config = PicturesqueState.get().config();
        if (config == null || !config.raytraceBloom) {
            return;
        }
        float strength = config.raytraceStrength;
        float vibrant = config.vibrantBoost;
        boolean changed = strength != lastStrength || vibrant != lastVibrant;
        if (!changed && tickCounter % SYNC_EVERY_TICKS != 0) {
            return;
        }
        lastStrength = strength;
        lastVibrant = vibrant;

        PostChain chain;
        try {
            chain = client.getShaderManager().getPostChain(RaytracePostPass.CHAIN_ID, LevelTargetBundle.MAIN_TARGETS);
        } catch (RuntimeException e) {
            warnOnce("could not fetch raytrace chain for uniform sync: " + e.getMessage());
            return;
        }
        if (chain == null) {
            return;
        }
        List<PostPass> passes;
        try {
            passes = ((PostChainAccessor) chain).picturesque$getPasses();
        } catch (Exception e) {
            return;
        }
        float[] values = new float[]{strength, vibrant, BLOOM_GAIN};
        for (PostPass pass : passes) {
            Map<String, GpuBuffer> uniforms;
            try {
                uniforms = ((PostPassAccessor) pass).picturesque$getCustomUniforms();
            } catch (Exception e) {
                continue;
            }
            GpuBuffer old = uniforms.get(UNIFORM_BLOCK);
            if (old == null || old.isClosed() || old.size() != (long) FLOAT_COUNT * 4L) {
                continue;
            }
            ByteBuffer data = ByteBuffer.allocateDirect(FLOAT_COUNT * 4).order(ByteOrder.nativeOrder());
            for (float value : values) {
                data.putFloat(value);
            }
            data.flip();
            GpuBuffer next;
            try {
                next = RenderSystem.getDevice().createBuffer(
                        () -> "lamplight:raytrace " + UNIFORM_BLOCK, GpuBuffer.USAGE_UNIFORM, data);
            } catch (Exception e) {
                warnOnce("could not allocate raytrace uniforms: " + e.getMessage());
                return;
            }
            uniforms.put(UNIFORM_BLOCK, next);
            RETIRED.add(new RetiredBuffer(old, tickCounter));
        }
        warned = false;
    }

    private static void drain() {
        while (!RETIRED.isEmpty() && RETIRED.peek().tick() < tickCounter - RETIRE_AFTER_TICKS) {
            try {
                RETIRED.poll().buffer().close();
            } catch (Exception e) {
            }
        }
    }

    private static void warnOnce(String message) {
        if (!warned) {
            warned = true;
            Picturesque.LOGGER.warn("[picturesque] {}", message);
        }
    }
}
