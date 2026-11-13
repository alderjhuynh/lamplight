package com.aura.lamplight.wayfarer.client.backpacktooltip.render;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import org.jspecify.annotations.NonNull;

public interface RenderContext {
	int x();
	int y();
	int viewportWidth();
	int viewportHeight();

	@NonNull
	GuiGraphicsExtractor graphics();

	@NonNull
	Font font();

	int mouseX();
	int mouseY();
	int tooltipTopX();
	int tooltipTopY();
}
