package com.aura.lamplight.wayfarer.client.backpacktooltip.render;

import com.aura.lamplight.wayfarer.client.backpacktooltip.BackpackTooltip;
import com.aura.lamplight.wayfarer.client.backpacktooltip.PreviewContext;
import com.aura.lamplight.wayfarer.client.backpacktooltip.PreviewProvider;
import com.aura.lamplight.wayfarer.client.backpacktooltip.PreviewType;
import com.aura.lamplight.wayfarer.client.backpacktooltip.TooltipType;

import org.jspecify.annotations.NonNull;

public interface PreviewRenderer {
	@NonNull
	static PreviewRenderer getDefaultRendererInstance() {
		return BackpackTooltip.INSTANCE.tooltipType() == TooltipType.VANILLA
				? VanillaPreviewRenderer.INSTANCE
				: ModPreviewRenderer.INSTANCE;
	}

	int getHeight();
	int getWidth();
	void setPreview(PreviewContext context, PreviewProvider provider);
	void setPreviewType(PreviewType type);
	void draw(RenderContext context);
}
