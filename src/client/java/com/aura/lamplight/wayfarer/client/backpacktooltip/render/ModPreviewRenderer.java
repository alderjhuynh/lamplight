package com.aura.lamplight.wayfarer.client.backpacktooltip.render;

import com.aura.lamplight.wayfarer.client.backpacktooltip.ColorKey;
import com.aura.lamplight.wayfarer.client.backpacktooltip.PreviewType;
import com.aura.lamplight.wayfarer.client.backpacktooltip.util.BackpackTooltipUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

@SuppressWarnings ("all")
public class ModPreviewRenderer extends BasePreviewRenderer {
	public static final ModPreviewRenderer INSTANCE = new ModPreviewRenderer();

	private static final Identifier DEFAULT_TEXTURE_LIGHT = BackpackTooltipUtil.id("backpack_tooltip");
	private static final Identifier SLOT_HIGHLIGHT_BACK_SPRITE = Identifier.withDefaultNamespace(
			"container/slot_highlight_back");
	private static final Identifier SLOT_HIGHLIGHT_FRONT_SPRITE = Identifier.withDefaultNamespace(
			"container/slot_highlight_front");

	ModPreviewRenderer() {
		super(18, 18, 8, 8);
	}

	@Override
	public int getWidth() {
		return 14 + Math.min(this.getMaxRowSize(), this.getInvSize()) * 18;
	}

	@Override
	public int getHeight() {
		return 14 + (int) Math.ceil(this.getInvSize() / (double) this.getMaxRowSize()) * 18;
	}

	private int getInvSize() {
		return this.previewType == PreviewType.COMPACT ?
				Math.max(1, this.compactItems.size()) :
				this.provider.getInventoryMaxSize(this.previewContext);
	}

	private int getColor() {
		if (!this.config.useColors()) {
			return 0xFF000000 | ColorKey.DEFAULT.rgb();
		}
		return this.provider.getWindowColor(this.previewContext);
	}

	private Identifier getTexture() {
		if (this.textureOverride != null)
			return this.textureOverride;
		return DEFAULT_TEXTURE_LIGHT;
	}

	private void drawBackground(int x, int y, GuiGraphicsExtractor graphics) {
		int invSize = this.getInvSize();
		int slotSize = 18;
		int padding = 14;

		int maxColumns = this.getMaxRowSize();

		int columns = Math.min(maxColumns, invSize);
		int rows = (int) Math.ceil(invSize / (double) maxColumns);

		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.getTexture(), x, y, padding + (columns * slotSize),
				padding + (rows * slotSize), this.getColor());
	}

	@Override
	public void draw(RenderContext context) {
		if (this.compactItems.isEmpty() || this.previewType == PreviewType.NO_PREVIEW)
			return;

		int viewportWidth = context.viewportWidth();
		int x = context.x();
		int y = context.y();
		var graphics = context.graphics();
		var font = context.font();
		int mouseX = context.mouseX();
		int mouseY = context.mouseY();

		this.drawBackground(x, y, graphics);
		if (this.previewType == PreviewType.FULL) {
			int maxSlots = this.previewContext != null ?
					this.provider.getInventoryMaxSize(this.previewContext) :
					Integer.MAX_VALUE;
			this.drawSlots(x, y, graphics, font, mouseX, mouseY, maxSlots - 1);
		} else {
			this.drawSlots(x, y, graphics, font, mouseX, mouseY, Integer.MAX_VALUE);
		}
		this.drawInnerTooltip(x, y, graphics, font, mouseX, mouseY);
	}

	@Override
	protected void drawSlot(ItemStack stack, int x, int y, GuiGraphicsExtractor graphics, Font font, int slot,
	                        boolean isHighlighted, boolean shortItemCount) {
		int maxRowSize = this.getMaxRowSize();
		int sx = this.slotXOffset + x + this.slotWidth * (slot % maxRowSize);
		int sy = this.slotYOffset + y + this.slotHeight * (slot / maxRowSize);

		if (isHighlighted) {
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_BACK_SPRITE, sx - 4, sy - 4, 24, 24);
		}

		if (!stack.isEmpty())
			this.drawItem(stack, sx, sy, graphics, font, shortItemCount);

		if (isHighlighted) {
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_FRONT_SPRITE, sx - 4, sy - 4, 24, 24);
		}
	}
}
