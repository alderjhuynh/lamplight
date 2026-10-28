package com.aura.lamplight.tinkerer.client;

import com.aura.lamplight.tinkerer.CharmEquipSlot;
import com.aura.lamplight.tinkerer.Tinkerer;
import com.aura.lamplight.tinkerer.charm.CharmDeckLayout;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

import java.util.List;

@SuppressWarnings ("all")
public class CharmDeckScreen extends InventoryScreen {

	private static final net.minecraft.resources.Identifier DECK_TEXTURE =
			Tinkerer.id("textures/gui/charm_deck.png");
	private static final int TEXTURE_WIDTH = 32;
	private static final int TEXTURE_HEIGHT = 86;
	private static final int TILE_V = 8;
	private static final int FOOTER_V = 62;

	private static final int PANE_GAP = 2;

	private static final WidgetSprites DECK_BUTTON_SPRITES = new WidgetSprites(
			Tinkerer.id("charm_deck/button"), Tinkerer.id("charm_deck/button_highlighted"));
	private static final int BUTTON_WIDTH = 20;
	private static final int BUTTON_HEIGHT = 18;

	private static boolean deckOpen = true;

	private final Player player;

	private ImageButton deckButton;

	public CharmDeckScreen(Player player) {
		super(player);
		this.player = player;
	}

	private List<CharmDeckLayout.Well> wells() {
		return CharmDeckLayout.wells(this.player, CharmDeckLayout.clientCapacities(),
				CharmDeckLayout.clientAcquired());
	}

	public static boolean isDeckOpen() {
		return deckOpen;
	}

	public static int effectsPaneAnchorX(CharmDeckScreen screen) {
		return screen.leftPos + CharmEquipSlot.DECK_ORIGIN_X + CharmDeckLayout.PANEL_WIDTH
				+ PANE_GAP;
	}

	@Override
	protected void init() {
		super.init();
		this.syncPaneVisibility();
		ScreenPosition buttonPos = this.getRecipeBookButtonPosition();
		this.deckButton = this.addRenderableWidget(new ImageButton(
				buttonPos.x() + BUTTON_WIDTH + PANE_GAP, buttonPos.y(),
				BUTTON_WIDTH, BUTTON_HEIGHT, DECK_BUTTON_SPRITES,
				button -> this.toggleDeck()));
	}

	private void toggleDeck() {
		this.deckOpen = !this.deckOpen;
		this.syncPaneVisibility();
		this.positionDeckButton();
	}

	@Override
	protected void onRecipeBookButtonClick() {
		super.onRecipeBookButtonClick();
		this.positionDeckButton();
	}

	private void positionDeckButton() {
		if (this.deckButton != null) {
			ScreenPosition buttonPos = this.getRecipeBookButtonPosition();
			this.deckButton.setPosition(buttonPos.x() + BUTTON_WIDTH + PANE_GAP,
					buttonPos.y());
		}
	}

	private void syncPaneVisibility() {
		for (Slot slot : this.getMenu().slots) {
			if (slot instanceof CharmEquipSlot charmSlot) {
				charmSlot.setPaneVisible(deckOpen);
			}
		}
	}

	@Override
	protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top) {
		if (!this.isDeckOpen()) {
			return super.hasClickedOutside(mouseX, mouseY, left, top);
		}
		boolean outside = super.hasClickedOutside(mouseX, mouseY, left, top);
		if (!outside) {
			return false;
		}
		List<CharmDeckLayout.Well> wells = this.wells();
		if (wells.isEmpty()) {
			return true;
		}
		int panelX = left + CharmEquipSlot.DECK_ORIGIN_X;
		int panelY = top + CharmEquipSlot.DECK_ORIGIN_Y;
		int panelHeight = CharmDeckLayout.panelHeight(wells.size());
		return !(mouseX >= panelX && mouseX < panelX + CharmDeckLayout.PANEL_WIDTH
				&& mouseY >= panelY && mouseY < panelY + panelHeight);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(gfx, mouseX, mouseY, partialTick);
		if (!this.isDeckOpen()) {
			return;
		}
		List<CharmDeckLayout.Well> wells = this.wells();
		if (wells.isEmpty()) {
			return;
		}
		int panelX = this.leftPos + CharmEquipSlot.DECK_ORIGIN_X;
		int panelY = this.topPos + CharmEquipSlot.DECK_ORIGIN_Y;
		gfx.blit(RenderPipelines.GUI_TEXTURED, DECK_TEXTURE, panelX, panelY,
				0.0F, 0.0F, CharmDeckLayout.PANEL_WIDTH, CharmDeckLayout.TOP_CAP,
				TEXTURE_WIDTH, TEXTURE_HEIGHT);
		for (int i = 0; i < wells.size() - 1; i++) {
			gfx.blit(RenderPipelines.GUI_TEXTURED, DECK_TEXTURE, panelX, panelY + CharmDeckLayout.wellY(i),
					0.0F, TILE_V, CharmDeckLayout.PANEL_WIDTH, CharmDeckLayout.WELL_PITCH,
					TEXTURE_WIDTH, TEXTURE_HEIGHT);
		}
		gfx.blit(RenderPipelines.GUI_TEXTURED, DECK_TEXTURE, panelX,
				panelY + CharmDeckLayout.wellY(wells.size() - 1),
				0.0F, FOOTER_V, CharmDeckLayout.PANEL_WIDTH, CharmDeckLayout.FOOTER_UNIT,
				TEXTURE_WIDTH, TEXTURE_HEIGHT);
	}
}
