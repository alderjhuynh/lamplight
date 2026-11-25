package com.aura.lamplight.nullbloom.client.god;

public final class GodSkyTables {
	private GodSkyTables() {
	}

	public static final int DAY_LENGTH = 24000;

	private static final float[][] SKY = {{133, 1}, {11867, 1}, {13670, 0}, {22330, 0}, {24133, 1}};

	private static final float[][] STARS = {
		{92, 0.037F}, {627, 0.0F}, {11373, 0.0F}, {11732, 0.016F}, {11959, 0.044F}, {12399, 0.143F}, {12729, 0.258F},
		{13228, 0.5F}, {22772, 0.5F}, {23032, 0.364F}, {23356, 0.225F}, {23758, 0.101F}, {24092, 0.037F}
	};

	private static final int[] SUNRISE_TICKS = {
		71, 310, 565, 730, 11270, 11397, 11522, 11690, 11929, 12243, 12358, 12512, 12613, 12732, 12841, 13035, 13252,
		13775, 13888, 14039, 14192, 21807, 21961, 22112, 22225, 22748, 22965, 23159, 23272, 23488, 23642, 23757
	};
	private static final int[] SUNRISE_ARGB = {
		1609540403, 703969843, 117167155, 16770355, 16770355, 83679283, 268028723, 703969843, 1609540403, -1310226637,
		-857440717, -371166669, -153261261, -19242189, -19440589, -321760973, -1043577037, 918435635, 532362547,
		163001139, 11744051, 11678515, 163001139, 532362547, 918435635, -1043577037, -321760973, -19440589,
		-19242189, -371166669, -857440717, -1310226637
	};

	public static double wrap(double ticks) {
		double t = ticks % DAY_LENGTH;
		return t < 0 ? t + DAY_LENGTH : t;
	}

	public static float skyMultiplier(double timeOfDay) {
		return wrapTrack(SKY, timeOfDay);
	}

	public static float starBrightness(double timeOfDay) {
		return wrapTrack(STARS, timeOfDay);
	}

	public static int sunriseColor(double timeOfDay) {
		double t = wrap(timeOfDay);
		int n = SUNRISE_TICKS.length;
		int hi = 0;
		while (hi < n && SUNRISE_TICKS[hi] <= t) {
			hi++;
		}
		int loIndex = (hi - 1 + n) % n;
		int hiIndex = hi % n;
		double loTick = SUNRISE_TICKS[loIndex] - (hi == 0 ? DAY_LENGTH : 0);
		double hiTick = SUNRISE_TICKS[hiIndex] + (hi == n ? DAY_LENGTH : 0);
		double f = hiTick == loTick ? 0.0 : (t - loTick) / (hiTick - loTick);
		return lerpArgb(SUNRISE_ARGB[loIndex], SUNRISE_ARGB[hiIndex], (float) Math.max(0.0, Math.min(1.0, f)));
	}

	private static float wrapTrack(float[][] keys, double timeOfDay) {
		double t = wrap(timeOfDay);
		if (t < keys[0][0]) {
			t += DAY_LENGTH;
		}
		for (int i = 1; i < keys.length; i++) {
			if (t <= keys[i][0]) {
				double f = (t - keys[i - 1][0]) / (keys[i][0] - keys[i - 1][0]);
				return (float) (keys[i - 1][1] + (keys[i][1] - keys[i - 1][1]) * f);
			}
		}
		return keys[keys.length - 1][1];
	}

	private static int lerpArgb(int a, int b, float t) {
		int out = 0;
		for (int shift = 24; shift >= 0; shift -= 8) {
			int ca = (a >>> shift) & 0xFF;
			int cb = (b >>> shift) & 0xFF;
			out |= Math.round(ca + (cb - ca) * t) << shift;
		}
		return out;
	}
}
