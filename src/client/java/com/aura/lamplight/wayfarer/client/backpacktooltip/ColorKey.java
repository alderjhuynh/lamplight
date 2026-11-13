package com.aura.lamplight.wayfarer.client.backpacktooltip;

import net.minecraft.world.item.DyeColor;

public enum ColorKey {
	DEFAULT(0xffffff, null),
	LEATHER(DyeColor.BROWN);

	private final int rgb;

	ColorKey(int fallbackRgb, DyeColor dye) {
		if (dye == null) {
			this.rgb = fallbackRgb;
		} else {
			this.rgb = dimToMinBrightness(dye.getTextureDiffuseColor());
		}
	}

	ColorKey(DyeColor dye) {
		this(0xffffff, dye);
	}

	public int rgb() {
		return rgb;
	}

	public static int dimToMinBrightness(int color) {
		float r = Math.max(0.15f, ((color >> 16) & 0xff) / 255f);
		float g = Math.max(0.15f, ((color >> 8) & 0xff) / 255f);
		float b = Math.max(0.15f, (color & 0xff) / 255f);
		return (toByte(r) << 16) | (toByte(g) << 8) | toByte(b);
	}

	private static int toByte(float v) {
		return Math.round(v * 255f);
	}
}
