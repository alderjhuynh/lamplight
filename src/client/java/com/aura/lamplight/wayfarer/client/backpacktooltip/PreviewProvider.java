package com.aura.lamplight.wayfarer.client.backpacktooltip;

import com.aura.lamplight.wayfarer.client.backpacktooltip.render.PreviewRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import org.jspecify.annotations.Nullable;
import java.util.Collections;
import java.util.List;

public interface PreviewProvider {
	boolean shouldDisplay(PreviewContext context);

	List<ItemStack> getInventory(PreviewContext context);

	int getInventoryMaxSize(PreviewContext context);

	default int getMaxRowSize(PreviewContext context) {
		return 0;
	}

	default int getCompactMaxRowSize(PreviewContext context) {
		return 0;
	}

	default boolean isFullPreviewAvailable(PreviewContext context) {
		return true;
	}

	default boolean showTooltipHints(PreviewContext context) {
		return true;
	}

	default ColorKey getWindowColorKey(PreviewContext context) {
		return ColorKey.DEFAULT;
	}

	default int getWindowColor(PreviewContext context) {
		return 0xFF000000 | getWindowColorKey(context).rgb();
	}

	@Nullable
	default Identifier getTextureOverride(PreviewContext context) {
		return null;
	}

	default PreviewRenderer getRenderer() {
		return PreviewRenderer.getDefaultRendererInstance();
	}

	default List<Component> addTooltip(PreviewContext context) {
		return Collections.emptyList();
	}
}
