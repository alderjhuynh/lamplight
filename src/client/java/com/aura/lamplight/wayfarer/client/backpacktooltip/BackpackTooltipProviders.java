package com.aura.lamplight.wayfarer.client.backpacktooltip;

import com.aura.lamplight.wayfarer.ModItems;

public final class BackpackTooltipProviders {
	private BackpackTooltipProviders() {
	}

	public static void register() {
		PreviewProviderRegistry registry = PreviewProviderRegistry.getInstance();

		registry.register(new BackpackPreviewProvider(), ModItems.LEATHER_BACKPACK);
	}
}
