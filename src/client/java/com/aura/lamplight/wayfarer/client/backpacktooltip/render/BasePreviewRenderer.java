package com.aura.lamplight.wayfarer.client.backpacktooltip.render;

import com.aura.lamplight.wayfarer.client.backpacktooltip.PreviewConfiguration;
import com.aura.lamplight.wayfarer.client.backpacktooltip.PreviewContext;
import com.aura.lamplight.wayfarer.client.backpacktooltip.PreviewProvider;
import com.aura.lamplight.wayfarer.client.backpacktooltip.PreviewType;
import com.aura.lamplight.wayfarer.client.backpacktooltip.hook.GuiGraphicsExtensions;
import com.aura.lamplight.wayfarer.client.backpacktooltip.util.BackpackTooltipUtil;
import com.aura.lamplight.wayfarer.client.backpacktooltip.util.MergedItemStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public abstract class BasePreviewRenderer implements PreviewRenderer {

	protected PreviewType previewType;
	protected PreviewConfiguration config;
	protected int compactMaxRowSize;
	protected int maxRowSize;
	protected Identifier textureOverride;
	protected PreviewProvider provider;
	protected List<ItemStack> fullItems;
	protected List<MergedItemStack> compactItems;
	protected PreviewContext previewContext;

	protected final int slotWidth;
	protected final int slotHeight;
	protected final int slotXOffset;
	protected final int slotYOffset;

	protected BasePreviewRenderer(int slotWidth, int slotHeight, int slotXOffset, int slotYOffset) {
		this.fullItems = List.of();
		this.compactItems = List.of();
		this.previewType = PreviewType.FULL;
		this.maxRowSize = 9;
		this.config = new PreviewConfiguration(9, true, false, true);
		this.provider = null;
		this.previewContext = null;

		this.slotWidth = slotWidth;
		this.slotHeight = slotHeight;
		this.slotXOffset = slotXOffset;
		this.slotYOffset = slotYOffset;
	}

	protected int getMaxRowSize() {
		return this.previewType == PreviewType.COMPACT ? this.compactMaxRowSize : this.maxRowSize;
	}

	@Override
	public void setPreviewType(PreviewType type) {
		this.previewType = type;
	}

	@Override
	public void setPreview(PreviewContext context, PreviewProvider provider) {
		int rowSize = provider.getMaxRowSize(context);
		int compactRowSize = provider.getCompactMaxRowSize(context);

		this.config = context.config();
		if (compactRowSize <= 0)
			compactRowSize = this.config.defaultMaxRowSize();
		if (compactRowSize <= 0)
			compactRowSize = 9;
		if (rowSize <= 0)
			rowSize = compactRowSize;
		this.maxRowSize = rowSize;
		this.compactMaxRowSize = compactRowSize;
		this.textureOverride = provider.getTextureOverride(context);
		this.provider = provider;
		this.fullItems = provider.getInventory(context);
		this.compactItems = MergedItemStack.mergeInventory(this.fullItems, provider.getInventoryMaxSize(context),
				this.config.mergeItems());
		this.previewContext = context;
	}

	protected int getSlotAt(int x, int y) {
		int slot = -1;

		if (x + 1 >= this.slotXOffset && y + 1 >= this.slotYOffset) {
			int maxRowSize = this.getMaxRowSize();
			int slotX = (x + 1 - this.slotXOffset) / this.slotWidth;
			int slotY = (y + 1 - this.slotYOffset) / this.slotHeight;

			if (slotX < maxRowSize)
				slot = slotX + slotY * maxRowSize;
		}

		return slot;
	}

	private ItemStack getStackAt(int x, int y) {
		int slot = this.getSlotAt(x, y);

		if (this.previewType == PreviewType.COMPACT) {
			if (slot < 0 || slot >= this.compactItems.size())
				return ItemStack.EMPTY;
			MergedItemStack merged = this.compactItems.get(slot);

			return merged == null ? ItemStack.EMPTY : merged.get();
		} else if (slot >= 0 && slot < this.fullItems.size()) {
			return this.fullItems.get(slot);
		}
		return ItemStack.EMPTY;
	}

	protected void drawSlots(int x, int y, GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY,
	                         int maxSlot) {
		int mouseHoveredSlot = this.getSlotAt(mouseX - x, mouseY - y);

		if (this.previewType == PreviewType.COMPACT) {
			boolean shortItemCounts = this.config.shortItemCounts();

			for (int slot = 0, size = this.compactItems.size(); slot < size; ++slot) {
				if (slot <= maxSlot)
					this.drawSlot(this.compactItems.get(slot).get(), x, y, graphics, font, slot,
							mouseHoveredSlot == slot, shortItemCounts);
			}
		} else {
			for (int slot = 0, size = this.fullItems.size(); slot < size; ++slot) {
				if (slot <= maxSlot)
					this.drawSlot(this.fullItems.get(slot), x, y, graphics, font, slot, mouseHoveredSlot == slot,
							false);
			}
		}
	}

	protected abstract void drawSlot(ItemStack stack, int x, int y, GuiGraphicsExtractor graphics, Font font, int slot,
	                                 boolean isHighlighted, boolean shortItemCount);

	protected void drawItem(ItemStack stack, int x, int y, GuiGraphicsExtractor graphics, Font font,
	                        boolean shortItemCount) {
		String countLabel = "";

		if (stack.getCount() != 1) {
			if (shortItemCount)
				countLabel = BackpackTooltipUtil.abbreviateInteger(stack.getCount());
			else
				countLabel = String.valueOf(stack.getCount());
		}

		graphics.item(stack, x, y);
		graphics.itemDecorations(font, stack, x, y, countLabel);
	}

	protected void drawInnerTooltip(int x, int y, GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
		ItemStack stack = this.getStackAt(mouseX - x, mouseY - y);

		if (stack.isEmpty())
			return;

		GuiGraphicsExtensions.renderTooltipImmediate(graphics,
				() -> graphics.setTooltipForNextFrame(font, stack, mouseX, mouseY));
	}
}
