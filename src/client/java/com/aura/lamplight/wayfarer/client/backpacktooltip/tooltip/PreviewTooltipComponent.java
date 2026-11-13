package com.aura.lamplight.wayfarer.client.backpacktooltip.tooltip;

import com.aura.lamplight.wayfarer.client.backpacktooltip.PreviewContext;
import com.aura.lamplight.wayfarer.client.backpacktooltip.PreviewProvider;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

/**
 * Tooltip data linking a {@link PreviewProvider} with the {@link PreviewContext}
 * it should render for. Consumed by {@link PreviewClientTooltipComponent} on the
 * client.
 */
public record PreviewTooltipComponent(PreviewProvider provider, PreviewContext context) implements TooltipComponent {
}
