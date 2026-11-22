package com.aura.lamplight.nullbloom.client;

import com.aura.lamplight.client.mixin.picturesque.raytrace.GameRendererAccessor;
import com.aura.lamplight.client.mixin.picturesque.raytrace.PostChainAccessor;
import com.aura.lamplight.client.mixin.picturesque.raytrace.PostPassAccessor;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NullbloomDarkness {
	public static final Identifier CHAIN_ID = Identifier.fromNamespaceAndPath("lamplight", "nullbloom_darkness");

	public static final float CENTER_X = 0.0F;
	public static final float CENTER_Z = 0.0F;
	public static final float GLOOMY_RADIUS = 5.0F;
	public static final float DARK_RADIUS = 10.0F;
	public static final float CENTER_BRIGHTNESS = 0.50F;
	public static final float GLOOMY_EDGE_BRIGHTNESS = 0.14F;
	public static final float DARK_FALLOFF_EXPONENT = 2.0F;

	public static final int DEBUG_MODE = readDebugMode();

	private static int readDebugMode() {
		String env = System.getenv("LAMPLIGHT_NULLBLOOM_DEBUG");
		if (env != null) {
			try {
				return Integer.parseInt(env.trim());
			} catch (NumberFormatException ignored) {
			}
		}
		return Integer.getInteger("lamplight.nullbloom.debug", 0);
	}

	private static final Logger LOGGER = LoggerFactory.getLogger("lamplight/nullbloom_darkness");
	private static final String UNIFORM_BLOCK = "DarknessConfig";
	private static final int RETIRE_AFTER_FRAMES = 8;
	private static final int CONFIG_SIZE = new Std140SizeCalculator()
		.putMat4f().putVec4().putVec4().putVec4().putVec4().get();

	private record Retired(GpuBuffer buffer, long frame) {
	}

	private static final Deque<Retired> RETIRED = new ArrayDeque<>();
	private static long frameCounter;
	private static boolean warned;

	private NullbloomDarkness() {
	}

	@SuppressWarnings("deprecation")
	public static void render(Minecraft client) {
		frameCounter++;
		drain();
		if (client.level == null || client.gameRenderer == null || !NullbloomClientState.inside() || DEBUG_MODE == 9) {
			return;
		}
		try {
			PostChain chain = client.getShaderManager().getPostChain(CHAIN_ID, LevelTargetBundle.MAIN_TARGETS);
			if (chain == null) {
				warnOnce("post chain missing or failed to compile; the nullbloom will be fully lit");
				return;
			}
			if (!pushUniforms(chain, client.gameRenderer.mainCamera())) {
				return;
			}
			GameRendererAccessor access = (GameRendererAccessor) client.gameRenderer;
			chain.process(access.picturesque$getMainRenderTarget(), access.picturesque$getResourcePool());
			warned = false;
		} catch (RuntimeException e) {
			warnOnce("post pass failed, skipping until it works again: " + e.getMessage());
		}
	}

	private static boolean pushUniforms(PostChain chain, Camera camera) {
		Matrix4f invViewProj = camera.getViewRotationProjectionMatrix(new Matrix4f()).invert();
		Vec3 cam = camera.position();
		boolean zeroToOne = RenderSystem.getDevice().getDeviceInfo().isZZeroToOne();

		List<PostPass> passes = ((PostChainAccessor) chain).picturesque$getPasses();
		boolean pushed = false;
		for (PostPass pass : passes) {
			Map<String, GpuBuffer> uniforms = ((PostPassAccessor) pass).picturesque$getCustomUniforms();
			GpuBuffer old = uniforms.get(UNIFORM_BLOCK);
			if (old == null || old.isClosed()) {
				continue;
			}
			if (old.size() != CONFIG_SIZE) {
				warnOnce("DarknessConfig size mismatch (json=" + old.size() + ", java=" + CONFIG_SIZE
					+ "). Keep nullbloom_darkness.json, nullbloom_darkness.fsh and NullbloomDarkness in sync.");
				return false;
			}
			GpuBuffer next;
			try (MemoryStack stack = MemoryStack.stackPush()) {
				Std140Builder b = Std140Builder.onStack(stack, CONFIG_SIZE);
				b.putMat4f(invViewProj);
				b.putVec4((float) cam.x, (float) cam.y, (float) cam.z, zeroToOne ? 1.0F : 0.0F);
				b.putVec4(CENTER_X, CENTER_Z, 0.0F, 0.0F);
				b.putVec4(GLOOMY_RADIUS, DARK_RADIUS, CENTER_BRIGHTNESS, GLOOMY_EDGE_BRIGHTNESS);
				b.putVec4(DARK_FALLOFF_EXPONENT, (float) DEBUG_MODE, 0.0F, 0.0F);
				next = RenderSystem.getDevice().createBuffer(
					() -> "lamplight:nullbloom_darkness " + UNIFORM_BLOCK, GpuBuffer.USAGE_UNIFORM, b.get());
			}
			uniforms.put(UNIFORM_BLOCK, next);
			RETIRED.add(new Retired(old, frameCounter));
			pushed = true;
		}
		return pushed;
	}

	private static void drain() {
		while (!RETIRED.isEmpty() && RETIRED.peek().frame() < frameCounter - RETIRE_AFTER_FRAMES) {
			GpuBuffer buffer = RETIRED.poll().buffer();
			if (!buffer.isClosed()) {
				buffer.close();
			}
		}
	}

	private static void warnOnce(String message) {
		if (!warned) {
			warned = true;
			LOGGER.warn("[nullbloom_darkness] {}", message);
		}
	}
}
