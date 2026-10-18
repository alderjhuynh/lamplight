package com.aura.lamplight.armory.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;

public class BackSlotTransformScreen extends Screen {
	private float previewMouseX;
	private float previewMouseY;

	public BackSlotTransformScreen() {
		super(Component.literal("Back Slot Transform"));
	}

	@Override
	protected void init() {
		BackSlotTransform transform = BackSlotTransform.get();

		addRenderableWidget(new StringWidget(0, 12, width, 12, Component.literal("Back Slot Transform"), font));
		addRenderableWidget(new StringWidget(0, 26, width, 10,
				Component.literal("save to apply to json"), font));

		int sliderX = width / 2 + 10;
		int sliderWidth = Math.max(120, Math.min(200, width / 2 - 40));
		int y = 48;
		addSlider(sliderX, y, sliderWidth, "Offset X", -1.0F, 1.0F, transform.offsetX, "%.2f", v -> transform.offsetX = v);
		y += 24;
		addSlider(sliderX, y, sliderWidth, "Offset Y", -1.0F, 1.0F, transform.offsetY, "%.2f", v -> transform.offsetY = v);
		y += 24;
		addSlider(sliderX, y, sliderWidth, "Offset Z", -1.0F, 1.0F, transform.offsetZ, "%.2f", v -> transform.offsetZ = v);
		y += 24;
		addSlider(sliderX, y, sliderWidth, "Rotation X", -180.0F, 180.0F, transform.rotX, "%.0f", v -> transform.rotX = v);
		y += 24;
		addSlider(sliderX, y, sliderWidth, "Rotation Y", -180.0F, 180.0F, transform.rotY, "%.0f", v -> transform.rotY = v);
		y += 24;
		addSlider(sliderX, y, sliderWidth, "Rotation Z", -180.0F, 180.0F, transform.rotZ, "%.0f", v -> transform.rotZ = v);
		y += 24;
		addSlider(sliderX, y, sliderWidth, "Scale", 0.1F, 2.0F, transform.scale, "%.2f", v -> transform.scale = v);

		int buttonY = height - 30;
		int buttonWidth = 90;
		int buttonsWidth = buttonWidth * 3 + 8;
		int buttonX = (width - buttonsWidth) / 2;
		addRenderableWidget(Button.builder(Component.literal("Save"),
				button -> BackSlotTransform.get().save())
				.bounds(buttonX, buttonY, buttonWidth, 20).build());
		addRenderableWidget(Button.builder(Component.literal("Reset"),
				button -> {
					BackSlotTransform.get().resetToDefaults();
					minecraft.gui.setScreen(new BackSlotTransformScreen());
				}).bounds(buttonX + buttonWidth + 4, buttonY, buttonWidth, 20).build());
		addRenderableWidget(Button.builder(Component.literal("Done"),
				button -> minecraft.gui.setScreen(null))
				.bounds(buttonX + (buttonWidth + 4) * 2, buttonY, buttonWidth, 20).build());
	}

	private void addSlider(int x, int y, int sliderWidth, String name, float min, float max, float current, String format, FloatSlider.FloatSetter setter) {
		addRenderableWidget(new FloatSlider(x, y, sliderWidth, name, min, max, current, format, setter));
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		previewMouseX = mouseX;
		previewMouseY = mouseY;
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(graphics, mouseX, mouseY, partialTick);
		if (minecraft.player == null) {
			return;
		}
		int x1 = 30;
		int y1 = 48;
		int x2 = width / 2 - 20;
		int y2 = height - 44;
		if (x2 <= x1 + 40 || y2 <= y1 + 40) {
			return;
		}
		int scale = Math.max(20, (y2 - y1) / 3);
		InventoryScreen.extractEntityInInventoryFollowsMouse(graphics, x1, y1, x2, y2, scale, 0.0625F,
				previewMouseX, previewMouseY, minecraft.player);
	}
}
