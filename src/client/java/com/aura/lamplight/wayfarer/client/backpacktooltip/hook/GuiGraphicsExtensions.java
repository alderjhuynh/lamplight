package com.aura.lamplight.wayfarer.client.backpacktooltip.hook;

import net.minecraft.client.gui.GuiGraphicsExtractor;

import org.jspecify.annotations.Nullable;

public interface GuiGraphicsExtensions {
	void setTooltipTopXPosition(int topX);

	int getTooltipTopXPosition();

	void setTooltipTopYPosition(int topY);

	int getTooltipTopYPosition();

	void setMouseX(int mouseX);

	int getMouseX();

	void setMouseY(int mouseY);

	int getMouseY();

	@Nullable Runnable getDeferredTooltip();

	void setDeferredTooltip(@Nullable Runnable deferredTooltip);

	static void renderTooltipImmediate(GuiGraphicsExtractor graphics, Runnable renderer) {
		var extendedGraphics = (GuiGraphicsExtensions) graphics;

		Runnable prevTooltip = extendedGraphics.getDeferredTooltip();
		extendedGraphics.setDeferredTooltip(null);

		renderer.run();

		Runnable currentTooltip = extendedGraphics.getDeferredTooltip();
		if (currentTooltip != null) {
			currentTooltip.run();
		}
		extendedGraphics.setDeferredTooltip(prevTooltip);
	}
}
