package com.aura.lamplight.tinkerer.client;

import com.aura.lamplight.client.mixin.tinkerer.RecipeBookScreenAccessor;
import com.aura.lamplight.tinkerer.CharmEquipSlot;
import com.aura.lamplight.tinkerer.Tinkerer;
import com.aura.lamplight.tinkerer.charm.CharmDeckLayout;
import com.aura.lamplight.wayfarer.atlas.AtlasPanel;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

	private static final int EFFECT_ICON = 18;
	private static final int EFFECT_STEP = 20;

	private static boolean deckOpen = true;

	private final Player player;

	private ImageButton deckButton;

	private EditBox atlasSearchBox;

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
		this.atlasSearchBox = this.addRenderableWidget(new EditBox(this.font,
				this.width - 200, this.height - 40, 180, 16, Component.empty()));
		this.atlasSearchBox.setHint(Component.translatable("gui.lamplight.atlas.search_hint"));
		this.atlasSearchBox.setVisible(false);
		AtlasPanel.setSearch(this.atlasSearchBox);
		AtlasPanel.attach(this);
	}

	public int guiLeft() {
		return this.leftPos;
	}

	public int guiTop() {
		return this.topPos;
	}

	public int guiWidth() {
		return this.imageWidth;
	}

	public int guiHeight() {
		return this.imageHeight;
	}

	public ScreenPosition bookButtonAnchor() {
		return this.getRecipeBookButtonPosition();
	}

	public boolean isBookOpen() {
		if (this instanceof RecipeBookScreenAccessor bookScreen) {
			return bookScreen.lamplight$recipeBookComponent().isVisible();
		}
		return false;
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
		if (AtlasPanel.contains(this, mouseX, mouseY)) {
			return false;
		}
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
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (AtlasPanel.contains(this, event.x(), event.y())
				&& AtlasPanel.handleClick(this, event.x(), event.y(), event.button())) {
			return true;
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (AtlasPanel.contains(this, mouseX, mouseY)
				&& AtlasPanel.handleScroll(this, mouseX, mouseY, scrollY)) {
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (this.atlasSearchBox != null && this.atlasSearchBox.isVisible()
				&& this.atlasSearchBox.isFocused()
				&& this.atlasSearchBox.keyPressed(event)) {
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	protected void extractTooltip(GuiGraphicsExtractor gfx, int mouseX, int mouseY) {
		if (AtlasPanel.contains(this, mouseX, mouseY)) {
			List<Component> lines = AtlasPanel.hoverLines(this.minecraft, mouseX, mouseY);
			if (lines != null && !lines.isEmpty()) {
				gfx.setTooltipForNextFrame(this.font, lines, Optional.empty(), mouseX, mouseY);
			}
			return;
		}
		this.extractEffectTooltip(gfx, mouseX, mouseY);
		super.extractTooltip(gfx, mouseX, mouseY);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(gfx, mouseX, mouseY, partialTick);
		AtlasPanel.extract(this, gfx, mouseX, mouseY);
		this.extractEffects(gfx);
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

	private record EffectCell(MobEffectInstance effect, int x, int y) {}

	private int effectColumnX() {
		int x = this.leftPos + this.imageWidth + 4;
		if (this.isDeckOpen()) {
			x = this.leftPos + CharmEquipSlot.DECK_ORIGIN_X + CharmDeckLayout.PANEL_WIDTH + 4;
		}
		return x;
	}

	private List<EffectCell> effectCells(final int limitX, final int limitY) {
		int x = this.effectColumnX();
		if (x + EFFECT_ICON > limitX) {
			return List.of();
		}
		List<EffectCell> cells = new ArrayList<>();
		int index = 0;
		for (MobEffectInstance effect : this.player.getActiveEffects()) {
			int y = this.topPos + index * EFFECT_STEP;
			if (y + EFFECT_ICON > limitY) {
				break;
			}
			cells.add(new EffectCell(effect, x, y));
			index++;
		}
		return cells;
	}

	private void extractEffects(final GuiGraphicsExtractor gfx) {
		int[] strip = AtlasPanel.stripBounds(this);
		int limitX;
		if (strip == null) {
			return;
		}
		limitX = strip[0] - 2;
		for (EffectCell cell : this.effectCells(limitX, this.height - 4)) {
			gfx.blitSprite(RenderPipelines.GUI_TEXTURED, Hud.getMobEffectSprite(cell.effect().getEffect()),
					cell.x(), cell.y(), EFFECT_ICON, EFFECT_ICON);
		}
	}

	private void extractEffectTooltip(final GuiGraphicsExtractor gfx, final int mouseX, final int mouseY) {
		int[] strip = AtlasPanel.stripBounds(this);
		if (strip == null) {
			return; 
		}
		for (EffectCell cell : this.effectCells(strip[0] - 2, this.height - 4)) {
			if (mouseX >= cell.x() && mouseY >= cell.y() && mouseX < cell.x() + EFFECT_ICON
					&& mouseY < cell.y() + EFFECT_ICON) {
				MobEffectInstance effect = cell.effect();
				net.minecraft.network.chat.MutableComponent name = effect.getEffect().value()
						.getDisplayName().copy();
				if (effect.getAmplifier() >= 1 && effect.getAmplifier() <= 9) {
					name.append(" ").append(Component.translatable("enchantment.level." + (effect.getAmplifier() + 1)));
				}
				Component duration = MobEffectUtil.formatDuration(effect, 1.0F,
						this.minecraft.level.tickRateManager().tickrate());
				gfx.setTooltipForNextFrame(this.font,
						List.of(name, Component.empty().append(duration).withStyle(ChatFormatting.GRAY)),
						Optional.empty(), mouseX, mouseY);
				return;
			}
		}
	}
}
