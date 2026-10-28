package com.aura.lamplight.tinkerer.client;

import com.aura.lamplight.tinkerer.JewelingTableMenu;
import com.aura.lamplight.tinkerer.JewelingSetPatternPayload;
import com.aura.lamplight.tinkerer.ModItems;
import com.aura.lamplight.tinkerer.Tinkerer;
import com.aura.lamplight.tinkerer.charm.CharmMaterial;
import com.aura.lamplight.tinkerer.charm.CharmMaterials;
import com.aura.lamplight.tinkerer.charm.CharmTemplate;
import com.aura.lamplight.tinkerer.charm.CharmTemplates;
import com.aura.lamplight.tinkerer.charm.CharmTemplates.TemplateEntry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector2ic;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JewelingTableScreen extends AbstractContainerScreen<JewelingTableMenu> {

	private static final Identifier BACKGROUND = Tinkerer.id("textures/gui/jeweling_table.png");
	private static final Identifier PATTERN_SPRITE = Tinkerer.id("tinkerer/pattern");
	private static final Identifier PATTERN_HOVER = Tinkerer.id("tinkerer/pattern_hover");
	private static final Identifier PATTERN_SELECTED = Tinkerer.id("tinkerer/pattern_selected");
	private static final Identifier SCROLLER = Tinkerer.id("tinkerer/scroller");
	private static final Identifier SCROLLER_DISABLED = Tinkerer.id("tinkerer/scroller_disabled");
	private static final Identifier CELL_SETTING = Tinkerer.id("tinkerer/cell_setting");
	private static final Identifier CELL_INSET = Tinkerer.id("tinkerer/cell_inset");

	private static final int SIDEBAR_X = 8;
	private static final int SIDEBAR_Y = 14;
	private static final int BUTTON_SIZE = 18;
	private static final int SCROLLBAR_X = 27;
	private static final int SCROLLBAR_WIDTH = 6;
	private static final int SCROLLBAR_HEIGHT = 27;
	private static final int SIDEBAR_ROWS = 8;
	private static final int SIDEBAR_RIGHT = SCROLLBAR_X + SCROLLBAR_WIDTH;

	private static final int CARD_GAP = 4;
	private static final int TOOLTIP_PAD = 12;

	private List<TemplateEntry> availableTemplates = List.of();
	private int scrollOff;
	@Nullable
	private TemplateEntry selected;
	private boolean draggingScrollbar;
	public JewelingTableScreen(JewelingTableMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, 210, 166);
		this.titleLabelX = 38;
		this.titleLabelY = 6;
		this.inventoryLabelX = 38;
		this.inventoryLabelY = 72;
		this.refreshAvailable();
		this.selected = menu.pattern() == null ? null : this.findEntry(menu.pattern());
		if (this.selected != null) {
			this.scrollOff = Math.min(this.indexOf(this.selected) / SIDEBAR_ROWS, this.maxScroll());
		}
	}

	public void refreshAvailable() {
		List<TemplateEntry> entries = new ArrayList<>();
		if (this.minecraft != null && this.minecraft.player != null) {
			for (TemplateEntry entry : CharmTemplates.ordered()) {
				if (CharmTemplates.isAvailable(this.minecraft.player, entry.def())) {
					entries.add(entry);
				}
			}
		}
		this.availableTemplates = List.copyOf(entries);
		this.scrollOff = Math.min(this.scrollOff, this.maxScroll());
		if (this.selected != null && !entries.contains(this.selected)) {
			this.selected = null;
		}
	}

	private CharmTemplates.@Nullable TemplateEntry findEntry(CharmTemplate def) {
		for (TemplateEntry entry : this.availableTemplates) {
			if (entry.def() == def) {
				return entry;
			}
		}
		return null;
	}

	private int indexOf(TemplateEntry entry) {
		return this.availableTemplates.indexOf(entry);
	}

	@Override
	protected void init() {
		super.init();
		this.refreshAvailable();
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		int mouseX = (int) event.x();
		int mouseY = (int) event.y();
		if (this.canScroll() && this.isOverScrollbar(mouseX, mouseY)) {
			this.draggingScrollbar = true;
			this.scrollFromMouse(mouseY);
			return true;
		}
		TemplateEntry pressed = this.sidebarEntryAt(mouseX, mouseY);
		if (pressed != null) {
			this.select(pressed);
			return true;
		}
		return super.mouseClicked(event, doubleClick);
	}

	private void select(TemplateEntry entry) {
		if (this.selected == entry) {
			this.selected = null;
			this.menu.setPattern(null);
			ClientPlayNetworking.send(new JewelingSetPatternPayload(this.menu.containerId, Optional.empty()));
			return;
		}
		this.selected = entry;
		this.menu.setPattern(entry.id());
		ClientPlayNetworking.send(new JewelingSetPatternPayload(this.menu.containerId, Optional.of(entry.id())));
	}

	public void applyAuthoritative(final int containerId, final Optional<Identifier> templateId) {
		if (this.minecraft == null || this.minecraft.player == null
				|| this.minecraft.player.containerMenu != this.menu
				|| this.menu.containerId != containerId) {
			return;
		}
		Identifier id = templateId.orElse(null);
		this.menu.setPattern(id);
		this.selected = id == null ? null : this.findEntryById(id);
	}

	private CharmTemplates.@Nullable TemplateEntry findEntryById(Identifier id) {
		for (TemplateEntry entry : this.availableTemplates) {
			if (entry.id().equals(id)) {
				return entry;
			}
		}
		return null;
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		this.draggingScrollbar = false;
		return super.mouseReleased(event);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (this.canScroll() && mouseX >= this.leftPos && mouseX < this.leftPos + SIDEBAR_RIGHT) {
			this.scrollOff = Mth.clamp(this.scrollOff - (int) Math.signum(scrollY), 0, this.maxScroll());
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
		if (this.draggingScrollbar) {
			this.scrollFromMouse((int) event.y());
			return true;
		}
		return super.mouseDragged(event, dx, dy);
	}

	private boolean canScroll() {
		return this.availableTemplates.size() > SIDEBAR_ROWS;
	}

	private int maxScroll() {
		return Math.max(0, this.availableTemplates.size() - SIDEBAR_ROWS);
	}

	private boolean isOverScrollbar(int mouseX, int mouseY) {
		return mouseX >= this.leftPos + SCROLLBAR_X && mouseX < this.leftPos + SIDEBAR_RIGHT
				&& mouseY >= this.topPos + SIDEBAR_Y && mouseY < this.topPos + SIDEBAR_Y + SIDEBAR_ROWS * BUTTON_SIZE;
	}

	private void scrollFromMouse(int mouseY) {
		int travel = this.maxScroll();
		if (travel <= 0) {
			return;
		}
		int channel = SIDEBAR_ROWS * BUTTON_SIZE - SCROLLBAR_HEIGHT;
		float ratio = (mouseY - (this.topPos + SIDEBAR_Y) - 13.5F) / channel;
		this.scrollOff = Mth.clamp(Math.round(ratio * travel), 0, travel);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(gfx, mouseX, mouseY, partialTick);
		gfx.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos, this.topPos,
				0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
		for (int i = 0; i < JewelingTableMenu.WORKSPACE_SIZE; i++) {
			Slot slot = this.menu.slots.get(i);
			if (!slot.isActive()) {
				continue;
			}
			Identifier cell = i == JewelingTableMenu.SETTING_INDEX ? CELL_SETTING : CELL_INSET;
			gfx.blitSprite(RenderPipelines.GUI_TEXTURED, cell,
					this.leftPos + slot.x - 3, this.topPos + slot.y - 3, 22, 22);
		}
	}

	@Override
	public void extractContents(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTick) {
		super.extractContents(gfx, mouseX, mouseY, partialTick);
		this.renderSidebar(gfx, mouseX, mouseY);
		if (this.menu.pattern() == null) {
			gfx.text(this.font, Component.translatable("gui.lamplight.jeweling.no_shape")
							.withStyle(ChatFormatting.DARK_GRAY),
					this.leftPos + 56, this.topPos + 56, 4210752);
		}
		this.renderPreview(gfx);
	}

	private void renderSidebar(GuiGraphicsExtractor gfx, int mouseX, int mouseY) {
		for (int i = 0; i < this.availableTemplates.size(); i++) {
			int row = i - this.scrollOff;
			if (row < 0 || row >= SIDEBAR_ROWS) {
				continue;
			}
			int x = this.leftPos + SIDEBAR_X;
			int y = this.topPos + SIDEBAR_Y + row * BUTTON_SIZE;
			boolean hovering = mouseX >= x && mouseX < x + BUTTON_SIZE && mouseY >= y && mouseY < y + BUTTON_SIZE;
			Identifier sprite = hovering ? PATTERN_HOVER
					: this.selected == this.availableTemplates.get(i) && this.menu.pattern() != null
					? PATTERN_SELECTED
					: PATTERN_SPRITE;
			gfx.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, BUTTON_SIZE, BUTTON_SIZE);
			ItemStack ghost = shapeIcon(this.availableTemplates.get(i).def());
			if (!ghost.isEmpty()) {
				gfx.fakeItem(ghost, x + 1, y + 1);
			}
		}
		Identifier scroller = this.canScroll() ? SCROLLER : SCROLLER_DISABLED;
		int y = this.scrollerY();
		gfx.blitSprite(RenderPipelines.GUI_TEXTURED, scroller, this.leftPos + SCROLLBAR_X,
				this.topPos + SIDEBAR_Y + y, SCROLLBAR_WIDTH, SCROLLBAR_HEIGHT);
	}

	private int scrollerY() {
		int travel = this.maxScroll();
		if (travel <= 0) {
			return 0;
		}
		int channel = SIDEBAR_ROWS * BUTTON_SIZE - SCROLLBAR_HEIGHT;
		return Math.round((float) this.scrollOff / travel * channel);
	}

	private static ItemStack shapeIcon(CharmTemplate template) {
		Item charm = ModItems.CHARMS.get(template.slotType());
		return charm == null ? ItemStack.EMPTY : new ItemStack(charm);
	}

	private void renderPreview(GuiGraphicsExtractor gfx) {
		if (this.menu.pattern() == null) {
			return;
		}
		List<Component> lines = new ArrayList<>();
		lines.add(Component.translatable(templateShapeKey(this.menu.pattern()))
				.withStyle(ChatFormatting.UNDERLINE, ChatFormatting.GOLD));

		ItemStack settingStack = this.menu.slots.get(JewelingTableMenu.SETTING_INDEX).getItem();
		CharmMaterial setting = CharmMaterials.byItem(settingStack);
		if (setting != null) {
			boolean paid = settingStack.getCount() >= setting.settingCost();
			lines.add(Component.translatable("gui.lamplight.jeweling.setting_row",
							settingStack.getHoverName(), count(settingStack.getCount(), setting.settingCost()))
					.withStyle(paid ? ChatFormatting.BLUE : ChatFormatting.RED));
		} else {
			lines.add(Component.translatable("gui.lamplight.jeweling.setting_empty")
					.withStyle(ChatFormatting.DARK_GRAY));
		}
		int insetCells = Math.min(this.menu.pattern().insetCount(), JewelingTableMenu.MAX_INSETS);
		for (int i = 0; i < insetCells; i++) {
			ItemStack stack = this.menu.slots.get(JewelingTableMenu.INSET_START + i).getItem();
			CharmMaterial material = CharmMaterials.byItem(stack);
			if (material != null) {
				boolean paid = stack.getCount() >= material.insetCost();
				lines.add(Component.translatable("gui.lamplight.jeweling.inset_row",
								stack.getHoverName(), count(stack.getCount(), material.insetCost()))
						.withStyle(paid ? ChatFormatting.BLUE : ChatFormatting.RED));
			} else {
				lines.add(Component.translatable("gui.lamplight.jeweling.inset_empty", i + 1)
						.withStyle(ChatFormatting.DARK_GRAY));
			}
		}
		ItemStack preview = this.menu.slots.get(JewelingTableMenu.RESULT_INDEX).getItem();
		if (!preview.isEmpty()) {
			lines.addAll(CharmTooltips.effectLines(preview));
		} else if (this.hasAnyMaterial(insetCells)) {
			lines.add(Component.empty());
			lines.add(Component.translatable("gui.lamplight.jeweling.incomplete")
					.withStyle(ChatFormatting.DARK_GRAY));
		}
		List<ClientTooltipComponent> card = new ArrayList<>();
		for (Component line : lines) {
			card.add(ClientTooltipComponent.create(line.getVisualOrderText()));
		}
		if (!preview.isEmpty()) {
			card.add(new PreviewItemComponent(preview));
		}
		int cardX = this.leftPos + this.imageWidth + CARD_GAP + TOOLTIP_PAD;
		int cardY = this.topPos + TOOLTIP_PAD;
		gfx.tooltip(this.font, card, cardX, cardY, new FixedCardPositioner(cardX, cardY), null);
	}

	private boolean hasAnyMaterial(int insetCells) {
		if (CharmMaterials.byItem(this.menu.slots.get(JewelingTableMenu.SETTING_INDEX).getItem()) != null) {
			return true;
		}
		for (int i = 0; i < insetCells; i++) {
			if (CharmMaterials.byItem(this.menu.slots.get(JewelingTableMenu.INSET_START + i).getItem()) != null) {
				return true;
			}
		}
		return false;
	}

	private record PreviewItemComponent(ItemStack stack) implements ClientTooltipComponent {

		private static final int ITEM_SIZE = 48;
		private static final int TOP_MARGIN = 12;
		private static final int BOTTOM_MARGIN = 0;

		@Override
		public int getHeight(final Font font) {
			return TOP_MARGIN + ITEM_SIZE + BOTTOM_MARGIN;
		}

		@Override
		public int getWidth(final Font font) {
			return ITEM_SIZE;
		}

		@Override
		public void extractImage(final Font font, final int x, final int y, final int cardWidth, final int cardHeight,
		                         final GuiGraphicsExtractor gfx) {
			gfx.pose().pushMatrix();
			gfx.pose().translate(x + (cardWidth - ITEM_SIZE) / 2.0F, y + TOP_MARGIN);
			gfx.pose().scale(3.0F, 3.0F);
			gfx.item(this.stack, 0, 0);
			gfx.pose().popMatrix();
		}
	}

	private record FixedCardPositioner(int positionX, int positionY) implements ClientTooltipPositioner {

		@Override
		public Vector2ic positionTooltip(final int guiWidth, final int guiHeight, final int anchorX,
		                                 final int anchorY, final int cardWidth, final int cardHeight) {
			return new org.joml.Vector2i(this.positionX, this.positionY);
		}
	}

	private static String count(int have, int need) {
		return have + "/" + need;
	}

	private static String templateShapeKey(CharmTemplate template) {
		Identifier id = CharmTemplates.idOf(template);
		if (id == null) {
			return "gui.lamplight.jeweling.preview";
		}
		return "template." + id.getNamespace() + "." + id.getPath();
	}

	@Override
	protected void extractTooltip(GuiGraphicsExtractor gfx, int mouseX, int mouseY) {
		super.extractTooltip(gfx, mouseX, mouseY);
		if (!this.menu.getCarried().isEmpty()) {
			return;
		}
		List<Component> lines = null;
		if (this.hoveredSlot == null) {
			TemplateEntry hovered = this.sidebarEntryAt(mouseX, mouseY);
			if (hovered != null) {
				lines = shapeTooltip(hovered);
			}
		} else if (this.menu.pattern() != null && !this.hoveredSlot.hasItem()
				&& this.menu.slots.indexOf(this.hoveredSlot) >= 0
				&& this.menu.slots.indexOf(this.hoveredSlot) < JewelingTableMenu.RESULT_INDEX
				&& this.hoveredSlot.isActive()) {
			lines = cellTooltip(this.menu.slots.indexOf(this.hoveredSlot));
		}
		if (lines != null && !lines.isEmpty()) {
			gfx.setTooltipForNextFrame(this.font, lines, Optional.empty(), mouseX, mouseY);
		}
	}

	private CharmTemplates.@Nullable TemplateEntry sidebarEntryAt(int mouseX, int mouseY) {
		for (int i = 0; i < this.availableTemplates.size(); i++) {
			int row = i - this.scrollOff;
			if (row < 0 || row >= SIDEBAR_ROWS) {
				continue;
			}
			int x = this.leftPos + SIDEBAR_X;
			int y = this.topPos + SIDEBAR_Y + row * BUTTON_SIZE;
			if (mouseX >= x && mouseX < x + BUTTON_SIZE && mouseY >= y && mouseY < y + BUTTON_SIZE) {
				return this.availableTemplates.get(i);
			}
		}
		return null;
	}

	private List<Component> shapeTooltip(TemplateEntry entry) {
		CharmTemplate template = entry.def();
		List<Component> lines = new ArrayList<>();
		lines.add(Component.translatable(templateShapeKey(template))
				.withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE));
		lines.add(Component.translatable("tooltip.lamplight.template_slot",
				Component.translatable("charm_slot_type.lamplight." + template.slotType()))
				.withStyle(ChatFormatting.GRAY));
		lines.add(Component.translatable("tooltip.lamplight.template_insets", template.insetCount())
				.withStyle(ChatFormatting.GRAY));
		if (template.focus() != 1.0) {
			lines.add(Component.translatable("tooltip.lamplight.template_focus",
					String.format("%.2f", template.focus())).withStyle(ChatFormatting.GRAY));
		}
		lines.add(Component.empty());
		if (template.unlockedByDefault()) {
			lines.add(Component.translatable("gui.lamplight.jeweling.always_available")
					.withStyle(ChatFormatting.BLUE));
		} else {
			Item mold = BuiltInRegistries.ITEM.getOptional(template.item()).orElse(null);
			Component moldName = mold == null
					? Component.literal(template.item().toString())
					: new ItemStack(mold).getHoverName();
			lines.add(Component.translatable("gui.lamplight.jeweling.mold_required", moldName)
					.withStyle(ChatFormatting.DARK_GRAY));
		}
		if (this.selected == entry) {
			lines.add(Component.translatable("gui.lamplight.jeweling.selected_hint")
					.withStyle(ChatFormatting.DARK_GRAY));
		}
		return lines;
	}

	private List<Component> cellTooltip(int index) {
		List<Component> lines = new ArrayList<>();
		if (index == JewelingTableMenu.SETTING_INDEX) {
			lines.add(Component.translatable("gui.lamplight.jeweling.setting_name")
					.withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE));
			lines.add(Component.translatable("gui.lamplight.jeweling.setting_desc")
					.withStyle(ChatFormatting.GRAY));
		} else {
			lines.add(Component.translatable("gui.lamplight.jeweling.inset_name", index)
					.withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE));
			lines.add(Component.translatable("gui.lamplight.jeweling.inset_desc")
					.withStyle(ChatFormatting.GRAY));
		}
		lines.add(Component.empty());
		for (CharmMaterial material : CharmMaterials.orderedMaterials()) {
			int cost = index == JewelingTableMenu.SETTING_INDEX
					? material.settingCost() : material.insetCost();
			Item materialItem = BuiltInRegistries.ITEM.getOptional(material.item()).orElse(null);
			Component name = materialItem == null
					? Component.literal(material.item().toString())
					: new ItemStack(materialItem).getHoverName();
			lines.add(Component.literal("  ×" + cost + " ").withStyle(ChatFormatting.DARK_GREEN)
					.append(name.copy().withStyle(ChatFormatting.GRAY)));
		}
		return lines;
	}
}
