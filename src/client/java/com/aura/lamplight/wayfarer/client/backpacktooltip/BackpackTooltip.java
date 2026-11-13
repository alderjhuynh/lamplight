package com.aura.lamplight.wayfarer.client.backpacktooltip;

public final class BackpackTooltip {
	public static final BackpackTooltip INSTANCE = new BackpackTooltip();

	private BackpackTooltip() {
	}

	public boolean isEnabled() {
		return true;
	}

	public PreviewConfiguration configuration() {
		return new PreviewConfiguration(3, true, false, true);
	}

	public TooltipType tooltipType() {
		return TooltipType.MOD;
	}

	public PreviewMode previewMode() {
		return PreviewMode.FULL;
	}
}
