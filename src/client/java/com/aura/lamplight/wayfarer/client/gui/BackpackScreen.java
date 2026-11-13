package com.aura.lamplight.wayfarer.client.gui;

import com.aura.lamplight.wayfarer.BackpackMenu;
import com.aura.lamplight.wayfarer.Wayfarer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class BackpackScreen extends AbstractContainerScreen<BackpackMenu> {

	private static final Identifier PANEL = Wayfarer.id("textures/gui/leather_backpack.png");

	private static final int IMAGE_WIDTH = 248;
	private static final int IMAGE_HEIGHT = 192;

	public BackpackScreen(BackpackMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, IMAGE_WIDTH, IMAGE_HEIGHT);
		this.titleLabelY = 4;
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(gfx, mouseX, mouseY, partialTick);
		gfx.blit(RenderPipelines.GUI_TEXTURED, PANEL, this.leftPos, this.topPos,
				0.0F, 0.0F, IMAGE_WIDTH, IMAGE_HEIGHT, 256, 256);
	}
}
