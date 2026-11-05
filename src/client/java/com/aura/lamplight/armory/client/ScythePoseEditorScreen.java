package com.aura.lamplight.armory.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;

public class ScythePoseEditorScreen extends Screen {
	private static final int PAGE_ARM = 0;
	private static final int PAGE_HOLD_RIGHT = 1;
	private static final int PAGE_HOLD_LEFT = 2;
	private static final String[] PAGE_NAMES = { "Arm", "Hold Right", "Hold Left", "First Person" };

	private final ScythePoseEditorStore store;
	private final int page;
	private float previewMouseX;
	private float previewMouseY;

	public ScythePoseEditorScreen(ScythePoseEditorStore store) {
		this(store, PAGE_ARM);
	}

	public ScythePoseEditorScreen(ScythePoseEditorStore store, int page) {
		super(Component.literal("Scythe Pose Editor"));
		this.store = store;
		this.page = page;
		store.applyToLive();
	}

	@Override
	protected void init() {
		addRenderableWidget(new StringWidget(0, 4, width, 12,
				Component.literal("Scythe Pose Editor"), font));
		addRenderableWidget(new StringWidget(0, 16, width, 10,
				Component.literal("save persists to config; report values to bake them"), font));

		int pageButtonWidth = 70;
		int pageButtonY = height - 64;
		int pageButtonX = 30;
		for (int i = 0; i < PAGE_NAMES.length; i++) {
			int index = i;
			Button pageButton = Button.builder(Component.literal(PAGE_NAMES[i]), button ->
					minecraft.gui.setScreen(new ScythePoseEditorScreen(store, index)))
					.bounds(pageButtonX, pageButtonY, pageButtonWidth, 18).build();
			pageButton.active = index != page;
			addRenderableWidget(pageButton);
			pageButtonX += pageButtonWidth + 4;
		}

		int sliderX = width / 2 + 10;
		int sliderWidth = Math.max(120, Math.min(200, width / 2 - 40));
		int y = 36;
		switch (page) {
			case PAGE_ARM -> {
				y = addSliders(sliderX, y, sliderWidth,
						new SliderRow("Arm Rot X", -90.0F, 90.0F, store.armXDeg, "%.1f", v -> store.armXDeg = v),
						new SliderRow("Arm Rot Y", -90.0F, 90.0F, store.armYDeg, "%.1f", v -> store.armYDeg = v),
						new SliderRow("Arm Rot Z", -90.0F, 90.0F, store.armZDeg, "%.1f", v -> store.armZDeg = v));
			}
			case PAGE_HOLD_RIGHT -> {
				y = addSliders(sliderX, y, sliderWidth,
						new SliderRow("Rot X", -180.0F, 180.0F, store.tpRotX, "%.0f", v -> store.tpRotX = v),
						new SliderRow("Rot Y", -180.0F, 180.0F, store.tpRotY, "%.0f", v -> store.tpRotY = v),
						new SliderRow("Rot Z", -180.0F, 180.0F, store.tpRotZ, "%.0f", v -> store.tpRotZ = v),
						new SliderRow("Trans X", -1.0F, 1.0F, store.tpTransX, "%.2f", v -> store.tpTransX = v),
						new SliderRow("Trans Y", -1.0F, 1.0F, store.tpTransY, "%.2f", v -> store.tpTransY = v),
						new SliderRow("Trans Z", -1.0F, 1.0F, store.tpTransZ, "%.2f", v -> store.tpTransZ = v),
						new SliderRow("Scale", 0.25F, 2.0F, store.tpScale, "%.2f", v -> store.tpScale = v));
			}
			case PAGE_HOLD_LEFT -> {
				y = addSliders(sliderX, y, sliderWidth,
						new SliderRow("Rot X", -180.0F, 180.0F, store.tpLeftRotX, "%.0f", v -> store.tpLeftRotX = v),
						new SliderRow("Rot Y", -180.0F, 180.0F, store.tpLeftRotY, "%.0f", v -> store.tpLeftRotY = v),
						new SliderRow("Rot Z", -180.0F, 180.0F, store.tpLeftRotZ, "%.0f", v -> store.tpLeftRotZ = v),
						new SliderRow("Trans X", -1.0F, 1.0F, store.tpLeftTransX, "%.2f", v -> store.tpLeftTransX = v),
						new SliderRow("Trans Y", -1.0F, 1.0F, store.tpLeftTransY, "%.2f", v -> store.tpLeftTransY = v),
						new SliderRow("Trans Z", -1.0F, 1.0F, store.tpLeftTransZ, "%.2f", v -> store.tpLeftTransZ = v),
						new SliderRow("Scale", 0.25F, 2.0F, store.tpLeftScale, "%.2f", v -> store.tpLeftScale = v));
			}
			default -> {
				y = addSliders(sliderX, y, sliderWidth,
						new SliderRow("Rot X", -180.0F, 180.0F, store.fpRotX, "%.0f", v -> store.fpRotX = v),
						new SliderRow("Rot Y", -180.0F, 180.0F, store.fpRotY, "%.0f", v -> store.fpRotY = v),
						new SliderRow("Rot Z", -180.0F, 180.0F, store.fpRotZ, "%.0f", v -> store.fpRotZ = v),
						new SliderRow("Trans X", -1.0F, 1.0F, store.fpTransX, "%.2f", v -> store.fpTransX = v),
						new SliderRow("Trans Y", -1.0F, 1.0F, store.fpTransY, "%.2f", v -> store.fpTransY = v),
						new SliderRow("Trans Z", -1.0F, 1.0F, store.fpTransZ, "%.2f", v -> store.fpTransZ = v),
						new SliderRow("Scale", 0.25F, 2.0F, store.fpScale, "%.2f", v -> store.fpScale = v));
			}
		}

		int buttonY = height - 30;
		int buttonWidth = 90;
		int buttonsWidth = buttonWidth * 3 + 8;
		int buttonX = (width - buttonsWidth) / 2;
		addRenderableWidget(Button.builder(Component.literal("Save"),
				button -> store.save())
				.bounds(buttonX, buttonY, buttonWidth, 20).build());
		addRenderableWidget(Button.builder(Component.literal("Reset"),
				button -> {
					store.resetToDefaults();
					store.applyToLive();
					minecraft.gui.setScreen(new ScythePoseEditorScreen(store, page));
				}).bounds(buttonX + buttonWidth + 4, buttonY, buttonWidth, 20).build());
		addRenderableWidget(Button.builder(Component.literal("Done"),
				button -> {
					store.save();
					minecraft.gui.setScreen(null);
				}).bounds(buttonX + (buttonWidth + 4) * 2, buttonY, buttonWidth, 20).build());
	}

	private int addSliders(int x, int y, int sliderWidth, SliderRow... rows) {
		for (SliderRow row : rows) {
			addSlider(x, y, sliderWidth, row.name(), row.min(), row.max(), row.current(), row.format(), row.setter());
			y += 24;
		}
		return y;
	}

	private record SliderRow(String name, float min, float max, float current, String format, FloatSlider.FloatSetter setter) {
	}

	private void addSlider(int x, int y, int sliderWidth, String name, float min, float max, float current, String format, FloatSlider.FloatSetter setter) {
		addRenderableWidget(new FloatSlider(x, y, sliderWidth, name, min, max, current, format, setter));
	}

	@Override
	public void removed() {
		store.applyToLive();
		super.removed();
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
		int y2 = height - 76;
		if (x2 <= x1 + 40 || y2 <= y1 + 40) {
			return;
		}
		int scale = Math.max(20, (y2 - y1) / 3);
		InventoryScreen.extractEntityInInventoryFollowsMouse(graphics, x1, y1, x2, y2, scale, 0.0625F,
				previewMouseX, previewMouseY, minecraft.player);
	}
}
