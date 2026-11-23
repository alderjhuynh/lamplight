package com.aura.lamplight.nullbloom.client.dialogue;

final class Motion {
	private Motion() {
	}

	static final long POP_MS = 190;
	static final float POP_START_SCALE = 1.9F;
	static final float POP_START_DROP = 3.0F;
	static final float POP_FADE_MS = 110.0F;
    static final float FLOAT_Y = 1.1F;
	static final float FLOAT_X = 0.55F;
	static final float DRIFT_Y = 0.7F;
	static final float ABERRATION_BASE = 0.35F;
	static final float ABERRATION_POP = 2.4F;
	static final float ABERRATION_ALPHA = 0.62F;
	static final float DISSOLVE_MS = 420.0F;
	static final float DISSOLVE_RISE = 2.5F;
	static final float[] TEXT = {0.88F, 0.88F, 0.90F};
	static final float[] RED = {1.00F, 0.16F, 0.24F};
	static final float[] BLUE = {0.16F, 0.68F, 1.00F};
	static float easeOutBack(float t) {
		float c1 = 1.70158F;
		float c3 = c1 + 1.0F;
		float u = t - 1.0F;
		return 1.0F + c3 * u * u * u + c1 * u * u;
	}

	static float clamp01(float v) {
		return v < 0.0F ? 0.0F : Math.min(v, 1.0F);
	}

}
