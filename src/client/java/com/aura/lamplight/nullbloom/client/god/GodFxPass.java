package com.aura.lamplight.nullbloom.client.god;

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

public final class GodFxPass {
	public static final Identifier CHAIN_ID = Identifier.fromNamespaceAndPath("lamplight", "god_vision");

	private static final Logger LOGGER = LoggerFactory.getLogger("lamplight/god_vision");
	private static final String UNIFORM_BLOCK = "GodConfig";
	private static final int RETIRE_AFTER_FRAMES = 8;
	private static final float TAU = (float) (Math.PI * 2.0);

	private static final int CONFIG_SIZE = new Std140SizeCalculator()
		.putMat4f()
		.putVec4().putVec4().putVec4().putVec4().putVec4().putVec4().putVec4()
		.get();

	private record Retired(GpuBuffer buffer, long frame) {
	}

	private static final Deque<Retired> RETIRED = new ArrayDeque<>();
	private static long frameCounter;
	private static boolean warned;

	private GodFxPass() {
	}

	@SuppressWarnings("deprecation")
	public static void render(Minecraft client) {
		frameCounter++;
		drain();
		if (client.level == null || client.gameRenderer == null) {
			return;
		}
		GodFxTimeline.Fx fx = GodSequence.fx();
		if (fx == null || fx.isIdentity()) {
			return;
		}
		try {
			PostChain chain = client.getShaderManager().getPostChain(CHAIN_ID, LevelTargetBundle.MAIN_TARGETS);
			if (chain == null) {
				warnOnce("post chain missing or failed to compile; God's world will look like the normal one");
				return;
			}
			if (!pushUniforms(chain, client, fx)) {
				return;
			}
			GameRendererAccessor access = (GameRendererAccessor) client.gameRenderer;
			chain.process(access.picturesque$getMainRenderTarget(), access.picturesque$getResourcePool());
			warned = false;
		} catch (RuntimeException e) {
			warnOnce("post pass failed, skipping until it works again: " + e.getMessage());
		}
	}

	private static boolean pushUniforms(PostChain chain, Minecraft client, GodFxTimeline.Fx fx) {
		Camera camera = client.gameRenderer.mainCamera();
		Matrix4f invViewProj = camera.getViewRotationProjectionMatrix(new Matrix4f()).invert();
		Vec3 cam = camera.position();
		boolean zeroToOne = RenderSystem.getDevice().getDeviceInfo().isZZeroToOne();

		float seconds = GodSequence.seconds();
		float speak = GodSequence.speakPulse();

		float d = fx.darkness();
		float ease = d * d * (3.0F - 2.0F * d);
		float breathe = 1.0F + 0.05F * d * (float) Math.sin(seconds * TAU / 7.0F);
		float tighten = (1.0F - 0.10F * speak * d) * breathe;
		float gloomy = lerp(150.0F, 7.0F, ease) * tighten;
		float dark = lerp(260.0F, 18.0F, ease) * tighten;
		float centerBrightness = lerp(1.0F, 0.60F, ease);
		float edgeBrightness = lerp(1.0F, 0.24F, ease);
		float farFloor = lerp(1.0F, 0.04F, ease);

		float centerRelX = (float) (GodSequence.centerX() - cam.x);
		float centerRelZ = (float) (GodSequence.centerZ() - cam.z);

		float dayDim = GodSequence.worldDimming(client.level, fx);

		List<PostPass> passes = ((PostChainAccessor) chain).picturesque$getPasses();
		boolean pushed = false;
		for (PostPass pass : passes) {
			Map<String, GpuBuffer> uniforms = ((PostPassAccessor) pass).picturesque$getCustomUniforms();
			GpuBuffer old = uniforms.get(UNIFORM_BLOCK);
			if (old == null || old.isClosed()) {
				continue;
			}
			if (old.size() != CONFIG_SIZE) {
				warnOnce("GodConfig size mismatch (json=" + old.size() + ", java=" + CONFIG_SIZE
					+ "). Keep god_vision.json, the god_*.fsh files and GodFxPass in sync.");
				return false;
			}
			GpuBuffer next;
			try (MemoryStack stack = MemoryStack.stackPush()) {
				Std140Builder b = Std140Builder.onStack(stack, CONFIG_SIZE);
				b.putMat4f(invViewProj);
				b.putVec4((float) cam.x, (float) cam.y, (float) cam.z, zeroToOne ? 1.0F : 0.0F);
				b.putVec4(centerRelX, centerRelZ, seconds, GodSequence.frameSeed());
				b.putVec4(gloomy, dark, centerBrightness, edgeBrightness);
				b.putVec4(fx.gray(), fx.contrast(), dayDim, fx.tint());
				b.putVec4(fx.aberration() + 0.25F * speak * fx.gray(), fx.pinch(), fx.ghost(), fx.glitch());
				b.putVec4(fx.grain(), fx.flash(), fx.vignette() + 0.10F * speak, fx.blink());
				b.putVec4(fx.skyDim(), farFloor, speak, 0.0F);
				next = RenderSystem.getDevice().createBuffer(
					() -> "lamplight:god_vision " + UNIFORM_BLOCK, GpuBuffer.USAGE_UNIFORM, b.get());
			}
			uniforms.put(UNIFORM_BLOCK, next);
			RETIRED.add(new Retired(old, frameCounter));
			pushed = true;
		}
		return pushed;
	}

	private static float lerp(float a, float b, float t) {
		return a + (b - a) * t;
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
			LOGGER.warn("[god_vision] {}", message);
		}
	}
}
