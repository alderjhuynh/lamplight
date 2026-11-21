package com.aura.lamplight.client.impactframe;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import com.aura.lamplight.client.mixin.picturesque.raytrace.GameRendererAccessor;
import com.aura.lamplight.client.mixin.picturesque.raytrace.PostChainAccessor;
import com.aura.lamplight.client.mixin.picturesque.raytrace.PostPassAccessor;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.Identifier;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// ignore this
public final class ImpactFrame {
    public static final Identifier CHAIN_ID = Identifier.fromNamespaceAndPath("lamplight", "impact_frame");

    private static final Logger LOGGER = LoggerFactory.getLogger("lamplight/impact_frame");
    private static final String UNIFORM_BLOCK = "ImpactConfig";
    private static final int RETIRE_AFTER_FRAMES = 8;
    private static final int CONFIG_SIZE = new Std140SizeCalculator()
            .putVec4().putVec4().putVec2()
            // Progress, Beat, Seed, FlickerMask
            .putFloat().putFloat().putFloat().putFloat()
            // Threshold, Softness, LocalContrast, Halftone
            .putFloat().putFloat().putFloat().putFloat()
            // InkWidth, InkLuma, InkDepth
            .putFloat().putFloat().putFloat()
            // LineCount, LineInner, LineWidth
            .putFloat().putFloat().putFloat()
            // Punch, Shake, ReleaseStart, FrontWidth
            .putFloat().putFloat().putFloat().putFloat()
            .get();

    public record Params(
            float durationSeconds,
            float beatsPerSecond,
            float centerU, float centerV,
            float[] dark, float[] light,
            int flickerMask,
            float threshold, float softness, float localContrast, float halftone,
            float inkWidth, float inkLuma, float inkDepth,
            int lineCount, float lineInner, float lineWidth,
            float punch, float shake,
            float releaseStart, float frontWidth) {

        public static Params defaults() {
            return builder().build();
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder toBuilder() {
            return new Builder()
                    .duration(durationSeconds).beatsPerSecond(beatsPerSecond)
                    .at(centerU, centerV)
                    .colors(dark, light)
                    .flickerMask(flickerMask)
                    .tone(threshold, softness, localContrast, halftone)
                    .ink(inkWidth, inkLuma, inkDepth)
                    .lines(lineCount, lineInner, lineWidth)
                    .punch(punch, shake)
                    .release(releaseStart, frontWidth);
        }

        public Params at(float u, float v) {
            return toBuilder().at(u, v).build();
        }

        public Params withDuration(float seconds) {
            return toBuilder().duration(seconds).build();
        }

        public Params withColors(float[] dark, float[] light) {
            return toBuilder().colors(dark, light).build();
        }
    }

    public static final class Builder {
        private float durationSeconds = 0.34f;
        private float beatsPerSecond = 24f;
        private float centerU = 0.5f;
        private float centerV = 0.5f;
        private float[] dark = {0f, 0f, 0f};
        private float[] light = {1f, 1f, 1f};
        private int flickerMask = 0b1011;
        private float threshold = 0.74f;
        private float softness = 0.021f;
        private float localContrast = 1.2f;
        private float halftone = 0.1f;
        private float inkWidth = 0.0025f;
        private float inkLuma = 0.62f;
        private float inkDepth = 0.06f;
        private int lineCount = 96;
        private float lineInner = 0.28f;
        private float lineWidth = 0.16f;
        private float punch = 0.05f;
        private float shake = 0.006f;
        private float releaseStart = 0.72f;
        private float frontWidth = 0.035f;

        private Builder() {
        }

        public Builder duration(float seconds) {
            this.durationSeconds = seconds;
            return this;
        }

        public Builder beatsPerSecond(float beats) {
            this.beatsPerSecond = beats;
            return this;
        }

        public Builder at(float u, float v) {
            this.centerU = u;
            this.centerV = v;
            return this;
        }

        public Builder colors(float[] dark, float[] light) {
            this.dark = dark;
            this.light = light;
            return this;
        }

        public Builder flickerMask(int mask) {
            this.flickerMask = mask;
            return this;
        }

       public Builder tone(float threshold, float softness, float localContrast, float halftone) {
            this.threshold = threshold;
            this.softness = softness;
            this.localContrast = localContrast;
            this.halftone = halftone;
            return this;
        }

        public Builder ink(float width, float luma, float depth) {
            this.inkWidth = width;
            this.inkLuma = luma;
            this.inkDepth = depth;
            return this;
        }

        public Builder lines(int count, float inner, float width) {
            this.lineCount = count;
            this.lineInner = inner;
            this.lineWidth = width;
            return this;
        }

        public Builder punch(float zoom, float shake) {
            this.punch = zoom;
            this.shake = shake;
            return this;
        }

        public Builder release(float start, float frontWidth) {
            this.releaseStart = start;
            this.frontWidth = frontWidth;
            return this;
        }

        public Params build() {
            return new Params(durationSeconds, beatsPerSecond, centerU, centerV, dark, light, flickerMask,
                    threshold, softness, localContrast, halftone,
                    inkWidth, inkLuma, inkDepth,
                    lineCount, lineInner, lineWidth,
                    punch, shake, releaseStart, frontWidth);
        }
    }

    private record Retired(GpuBuffer buffer, long frame) {
    }

    private static final Deque<Retired> RETIRED = new ArrayDeque<>();

    private static Params params;
    private static long startNanos;
    private static float seed;
    private static boolean active;
    private static long frameCounter;
    private static boolean warned;

    private ImpactFrame() {
    }

    public static void trigger() {
        trigger(Params.defaults());
    }

    public static void trigger(Params p) {
        params = p;
        startNanos = System.nanoTime();
        seed = ThreadLocalRandom.current().nextInt(100_000);
        active = true;
    }

    public static void clear() {
        active = false;
    }

    public static boolean isActive() {
        return active;
    }

    public static float progress() {
        if (!active || params == null) {
            return 0f;
        }
        return Math.min(1f, Math.max(0f, elapsedSeconds() / Math.max(params.durationSeconds(), 1.0e-3f)));
    }

    private static float elapsedSeconds() {
        return (System.nanoTime() - startNanos) / 1.0e9f;
    }

    @SuppressWarnings("deprecation")
    public static void render(Minecraft client) {
        frameCounter++;
        drain(false);
        if (!active || client.level == null || client.gameRenderer == null) {
            return;
        }
        float seconds = elapsedSeconds();
        float progress = Math.min(1f, Math.max(0f, seconds / Math.max(params.durationSeconds(), 1.0e-3f)));
        if (progress >= 1f) {
            clear();
            return;
        }
        try {
            PostChain chain = client.getShaderManager().getPostChain(CHAIN_ID, LevelTargetBundle.MAIN_TARGETS);
            if (chain == null) {
                warnOnce("impact_frame post chain missing or failed to compile");
                return;
            }
            float beat = (float) Math.floor(seconds * params.beatsPerSecond());
            if (!pushUniforms(chain, progress, beat)) {
                return;
            }
            GameRendererAccessor access = (GameRendererAccessor) client.gameRenderer;
            chain.process(access.picturesque$getMainRenderTarget(), access.picturesque$getResourcePool());
            warned = false;
        } catch (RuntimeException e) {
            warnOnce("impact_frame post pass failed, skipping until it works again: " + e.getMessage());
        }
    }

    private static boolean pushUniforms(PostChain chain, float progress, float beat) {
        List<PostPass> passes = ((PostChainAccessor) chain).picturesque$getPasses();
        boolean pushed = false;
        for (PostPass pass : passes) {
            Map<String, GpuBuffer> uniforms = ((PostPassAccessor) pass).picturesque$getCustomUniforms();
            GpuBuffer old = uniforms.get(UNIFORM_BLOCK);
            if (old == null || old.isClosed()) {
                continue;
            }
            if (old.size() != CONFIG_SIZE) {
                warnOnce("ImpactConfig size mismatch (json=" + old.size() + ", java=" + CONFIG_SIZE
                        + "). Keep impact_frame.json, impact_frame.fsh and ImpactFrame.writeConfig in sync.");
                return false;
            }
            GpuBuffer next;
            try (MemoryStack stack = MemoryStack.stackPush()) {
                Std140Builder builder = Std140Builder.onStack(stack, CONFIG_SIZE);
                writeConfig(builder, params, progress, beat, seed);
                next = RenderSystem.getDevice().createBuffer(
                        () -> "lamplight:impact_frame " + UNIFORM_BLOCK, GpuBuffer.USAGE_UNIFORM, builder.get());
            }
            uniforms.put(UNIFORM_BLOCK, next);
            RETIRED.add(new Retired(old, frameCounter));
            pushed = true;
        }
        return pushed;
    }

    private static void writeConfig(Std140Builder b, Params p, float progress, float beat, float seed) {
        b.putVec4(p.dark()[0], p.dark()[1], p.dark()[2], 1f);
        b.putVec4(p.light()[0], p.light()[1], p.light()[2], 1f);
        b.putVec2(p.centerU(), p.centerV());
        b.putFloat(progress);
        b.putFloat(beat);
        b.putFloat(seed);
        b.putFloat(p.flickerMask());
        b.putFloat(p.threshold());
        b.putFloat(p.softness());
        b.putFloat(p.localContrast());
        b.putFloat(p.halftone());
        b.putFloat(p.inkWidth());
        b.putFloat(p.inkLuma());
        b.putFloat(p.inkDepth());
        b.putFloat(p.lineCount());
        b.putFloat(p.lineInner());
        b.putFloat(p.lineWidth());
        b.putFloat(p.punch());
        b.putFloat(p.shake());
        b.putFloat(p.releaseStart());
        b.putFloat(p.frontWidth());
    }

    private static void drain(boolean all) {
        while (!RETIRED.isEmpty() && (all || RETIRED.peek().frame() < frameCounter - RETIRE_AFTER_FRAMES)) {
            GpuBuffer buffer = RETIRED.poll().buffer();
            if (!buffer.isClosed()) {
                buffer.close();
            }
        }
    }

    private static void warnOnce(String message) {
        if (!warned) {
            warned = true;
            LOGGER.warn("[impact_frame] {}", message);
        }
    }
}
