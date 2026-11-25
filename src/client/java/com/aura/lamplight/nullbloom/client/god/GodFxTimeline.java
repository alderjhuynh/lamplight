package com.aura.lamplight.nullbloom.client.god;

import com.aura.lamplight.nullbloom.GodCallTiming;

public final class GodFxTimeline {
	private GodFxTimeline() {
	}

	public enum Mode {
		FULL,
		BRIEF
	}

	public record Fx(
		float gray, float contrast, float tint,
		float aberration, float pinch, float ghost, float glitch,
		float grain, float flash, float vignette, float blink,
		float skyDim, float darkness,
		boolean godSky,
		boolean timeWarp,
		float bodyCycles,
		float lightCycles
	) {
		public boolean isIdentity() {
			return gray < 0.002F && darkness < 0.002F && aberration < 0.002F && glitch < 0.002F && grain < 0.002F
				&& flash < 0.002F && blink < 0.002F && vignette < 0.002F && ghost < 0.002F && Math.abs(pinch) < 0.002F;
		}
	}

	public static final int BODY_CYCLES = 14;
	public static final int LIGHT_CYCLES = 4;
	public static final int GLITCH_LEAD_TICKS = 16;
	public static final int FLASH = GodCallTiming.FLASH_TICKS;

	private static final float[][] GLITCH_ENV = {
		{0.00F, 0.00F}, {0.10F, 0.04F}, {0.20F, 0.37F}, {0.45F, 0.76F}, {0.80F, 1.00F}, {0.90F, 0.69F},
		{1.05F, 0.31F}, {1.25F, 0.40F}, {1.30F, 0.12F}, {1.40F, 0.51F}, {1.55F, 0.24F}, {1.65F, 0.16F},
		{2.05F, 0.16F}, {2.15F, 0.34F}, {2.40F, 0.32F}, {2.60F, 0.00F}
	};

	public static float glitchEnvelope(float seconds) {
		if (seconds <= 0.0F || seconds >= GLITCH_ENV[GLITCH_ENV.length - 1][0]) {
			return 0.0F;
		}
		for (int i = 1; i < GLITCH_ENV.length; i++) {
			if (seconds <= GLITCH_ENV[i][0]) {
				float t0 = GLITCH_ENV[i - 1][0];
				float t1 = GLITCH_ENV[i][0];
				return lerp(GLITCH_ENV[i - 1][1], GLITCH_ENV[i][1], (seconds - t0) / (t1 - t0));
			}
		}
		return 0.0F;
	}

	public static Fx sample(Mode mode, float tick, float endTick, long seed, float distortion) {
		float gray;
		float darkness;
		float aberration;
		float ghost;
		float pinch = 0.0F;
		float glitch;
		float grain;
		float vignette;
		float flash = 0.0F;
		float blink = 0.0F;
		float skyDim = 1.0F;
		boolean godSky = false;
		boolean timeWarp = false;
		float bodyCycles = 0.0F;
		float lightCycles = 0.0F;
		float breathe = (float) Math.sin(tick / 20.0F * (Math.PI * 2.0 / 7.0));

		if (mode == Mode.FULL && tick < FLASH) {
			float bell = 0.0F;
			float pulse = 0.0F;
			for (int toll : GodCallTiming.TOLL_TICKS) {
				bell += smoothstep(toll, toll + 12, tick);
				if (tick >= toll) {
					pulse = Math.max(pulse, smoothstep(toll, toll + 2, tick) * (float) Math.exp(-(tick - toll) / 9.0F));
				}
			}
			bell /= GodCallTiming.TOLL_TICKS.length;
			float u = clamp01((tick - GodCallTiming.TIMELAPSE_START_TICKS) / GodCallTiming.TIMELAPSE_TICKS);
			gray = bell * 0.82F + u * 0.18F;
			darkness = bell * 0.65F + 0.35F * (float) Math.pow(u, 1.5);
			aberration = 0.10F * bell + 0.45F * u * u + 0.35F * pulse;
			ghost = 0.10F * bell + 0.50F * u;
			pinch = -0.05F * pulse - 0.10F * u * u;
			grain = 0.08F * bell + 0.30F * u;
			vignette = 0.20F * bell + 0.25F * u;
			glitch = u * u * u * 0.5F;
			if (u > 0.0F) {
				timeWarp = true;
				bodyCycles = Math.round(BODY_CYCLES * distortion) * u * u * u;
				lightCycles = Math.round(LIGHT_CYCLES * distortion) * u * u;
			}
			glitch = Math.max(glitch, 0.9F * glitchEnvelope((tick - (FLASH - GLITCH_LEAD_TICKS)) / 20.0F));
			flash = smoothstep(FLASH - 3, FLASH, tick);
		} else {
			float a = mode == Mode.FULL ? tick - FLASH : tick;
			float glitchEnv;
			if (mode == Mode.FULL) {
				gray = 1.0F;
				darkness = 1.0F;
				flash = (float) Math.exp(-Math.max(a, 0.0F) / 7.0F);
				glitchEnv = glitchEnvelope((tick - (FLASH - GLITCH_LEAD_TICKS)) / 20.0F);
				godSky = true;
				skyDim = 0.85F;
			} else {
				gray = smoothstep(4, 24, tick);
				darkness = smoothstep(0, 30, tick);
				glitchEnv = glitchEnvelope(tick / 20.0F);
			}
			float micro = microGlitch(a, seed);
			glitch = Math.max(0.9F * glitchEnv, micro * 0.55F);
			aberration = gray * (0.22F + 0.04F * breathe) + 0.7F * glitchEnv;
			ghost = gray * 0.20F + 0.20F * glitchEnv;
			pinch = 0.03F * breathe * gray - 0.04F * glitchEnv;
			grain = 0.35F * gray;
			vignette = 0.45F * gray;
		}

		if (endTick >= 0.0F) {
			float e = endTick;
			float glitchEnv = glitchEnvelope(e / 20.0F);
			float fade = 1.0F - smoothstep(19, 65, e);
			gray = fade;
			darkness = fade;
			aberration = fade * 0.22F + 0.7F * glitchEnv;
			ghost = fade * 0.20F + 0.20F * glitchEnv;
			grain = 0.35F * fade;
			vignette = 0.45F * fade;
			pinch = -0.04F * glitchEnv;
			glitch = 0.9F * glitchEnv;
			flash = 0.0F;
			timeWarp = false;
			bodyCycles = 0.0F;
			lightCycles = 0.0F;
			if (mode == Mode.FULL) {
				blink = smoothstep(13, 18, e) - smoothstep(19, 27, e);
				godSky = e < 18.5F;
				skyDim = 0.85F;
			} else {
				godSky = false;
			}
		}

		float d = distortion;
		return new Fx(
			gray, 1.0F + 0.12F * gray, 0.80F,
			aberration * d, pinch * d, ghost * d, glitch * d,
			grain, flash * (0.5F + 0.5F * d), vignette, blink,
			skyDim, darkness,
			godSky, timeWarp, bodyCycles, lightCycles
		);
	}

	private static float microGlitch(float ticks, long seed) {
		if (ticks < 0.0F) {
			return 0.0F;
		}
		final int bucketLength = 160;
		long bucket = (long) Math.floor(ticks / bucketLength);
		float start = hash01(seed, bucket, 1) * (bucketLength - 14);
		float duration = 5.0F + hash01(seed, bucket, 2) * 6.0F;
		float into = ticks - bucket * bucketLength - start;
		if (bucket < 1 || into < 0.0F || into > duration) {
			return 0.0F;
		}
		return (float) Math.sin(Math.PI * into / duration);
	}

	private static float hash01(long seed, long bucket, int salt) {
		long h = seed * 0x9E3779B97F4A7C15L + bucket * 0xC2B2AE3D27D4EB4FL + salt * 0x165667B19E3779F9L;
		h ^= h >>> 29;
		h *= 0xBF58476D1CE4E5B9L;
		h ^= h >>> 32;
		return (h & 0xFFFFFF) / (float) 0x1000000;
	}

	public static float smoothstep(float edge0, float edge1, float x) {
		float t = clamp01((x - edge0) / (edge1 - edge0));
		return t * t * (3.0F - 2.0F * t);
	}

	public static float clamp01(float x) {
		return x < 0.0F ? 0.0F : Math.min(x, 1.0F);
	}

	public static float lerp(float a, float b, float t) {
		return a + (b - a) * t;
	}
}
