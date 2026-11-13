package com.aura.lamplight.wayfarer.client.backpacktooltip;

import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;

import org.jspecify.annotations.Nullable;
import java.util.Collection;
import java.util.function.Consumer;

public final class BackpackTooltipApi {
	private BackpackTooltipApi() {
	}

	@Nullable
	public static PreviewProvider getPreviewProviderForStack(ItemStack stack) {
		return PreviewProviderRegistry.getInstance().get(stack);
	}

	@Nullable
	public static PreviewProvider getProviderIfPreviewAvailable(PreviewContext context) {
		if (!BackpackTooltip.INSTANCE.isEnabled())
			return null;
		ItemStack stack = context.stack();
		TooltipDisplay tooltipDisplay = stack.getComponents().get(DataComponents.TOOLTIP_DISPLAY);

		if (tooltipDisplay != null && tooltipDisplay.hideTooltip()) {
			return null;
		}
		PreviewProvider provider = getPreviewProviderForStack(stack);

		if (provider != null && provider.shouldDisplay(context) && getCurrentPreviewType(
				provider.isFullPreviewAvailable(context)) != PreviewType.NO_PREVIEW) {
			return provider;
		}
		return null;
	}

	@Nullable
	public static PreviewType getCurrentPreviewType(boolean hasFullPreviewMode) {
		BackpackTooltip module = BackpackTooltip.INSTANCE;

		if (!module.isEnabled())
			return PreviewType.NO_PREVIEW;
		return switch (module.previewMode()) {
			case FULL -> hasFullPreviewMode ? PreviewType.FULL : PreviewType.COMPACT;
			case COMPACT -> PreviewType.COMPACT;
		};
	}

	public static void modifyStackTooltip(ItemStack stack, Consumer<Collection<Component>> tooltip) {
		BackpackTooltip module = BackpackTooltip.INSTANCE;

		if (!module.isEnabled())
			return;

		Minecraft client = Minecraft.getInstance();
		PreviewContext context = PreviewContext.builder(stack).withOwner(client.player).build();
		TooltipDisplay tooltipDisplay = stack.getComponents().get(DataComponents.TOOLTIP_DISPLAY);
		PreviewProvider provider = getPreviewProviderForStack(stack);

		if (provider == null || (tooltipDisplay != null && tooltipDisplay.hideTooltip()))
			return;
		if (provider.showTooltipHints(context)) {
			if (module.tooltipType() == TooltipType.MOD) {
				tooltip.accept(provider.addTooltip(context));
			}
		}
	}
}
