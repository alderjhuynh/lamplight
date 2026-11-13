package com.aura.lamplight.wayfarer.client.backpacktooltip.tooltip;

import com.aura.lamplight.wayfarer.client.backpacktooltip.PreviewContext;
import com.aura.lamplight.wayfarer.client.backpacktooltip.PreviewProvider;
import com.aura.lamplight.wayfarer.client.backpacktooltip.BackpackTooltipApi;
import com.aura.lamplight.wayfarer.client.backpacktooltip.hook.GuiGraphicsExtensions;
import com.aura.lamplight.wayfarer.client.backpacktooltip.render.PreviewRenderer;
import com.aura.lamplight.wayfarer.client.backpacktooltip.render.RenderContextImpl;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import org.jspecify.annotations.NonNull;

/**
 * Client-side counterpart of {@link PreviewTooltipComponent}: configures the
 * {@link PreviewRenderer} from the tooltip data and draws it inside the item
 * tooltip.
 */
public class PreviewClientTooltipComponent implements ClientTooltipComponent {
	private final PreviewRenderer renderer;

	/**
	 * Resolves the renderer (defaulting to the configured one), then
	 * feeds it the preview context and current preview type.
	 *
	 * @param data the tooltip data describing the provider and context
	 */
	public PreviewClientTooltipComponent(PreviewTooltipComponent data) {
		PreviewRenderer renderer = data.provider().getRenderer();

		if (renderer == null)
			renderer = PreviewRenderer.getDefaultRendererInstance();
		this.renderer = renderer;
		PreviewProvider provider = data.provider();
		PreviewContext context = data.context();

		renderer.setPreview(context, provider);
		renderer.setPreviewType(BackpackTooltipApi.getCurrentPreviewType(provider.isFullPreviewAvailable(context)));
	}

	/** @return the preview height, plus 4px of vertical breathing room */
	@Override
	public int getHeight(@NonNull Font font) {
		return this.renderer.getHeight() + 4;
	}

	/** @return the preview width */
	@Override
	public int getWidth(@NonNull Font font) {
		return this.renderer.getWidth();
	}

	/** Draws the preview using the hover position captured by the GUI mixin. */
	@Override
	public void extractImage(@NonNull Font font, int x, int y, int totalWidth, int totalHeight,
	                         @NonNull GuiGraphicsExtractor graphics) {
		var extendedGraphics = (GuiGraphicsExtensions) graphics;
		int mouseX = extendedGraphics.getMouseX();
		int mouseY = extendedGraphics.getMouseY();

		this.renderer.draw(new RenderContextImpl(x, y, totalWidth, this.renderer.getHeight(), graphics, font, mouseX,
				mouseY, x, y));
	}
}
