package com.aura.lamplight.wayfarer.atlas;

import com.aura.lamplight.tinkerer.charm.CharmTemplate;
import com.aura.lamplight.tinkerer.client.CharmDeckScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import org.jspecify.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

@SuppressWarnings ("all")
public final class AtlasPanel {

	private static final int CELL = 20;
	private static final int COLUMNS_MAX = 9;
	private static final int COLUMNS_MIN = 5;
	private static final int MARGIN = 2;
	private static final int SEARCH_TOTAL = 22;

	private static final int HOVER = 0x28FFFFFF;
	private static final int LOCKED = 0x60000000;
	private static final int TEXT_MAIN = 0xFFE6E6E6;
	private static final int TEXT_DIM = 0xFF969696;
	private static final int TEXT_CARD = 0xFF303030;

	private static int listRow;

	private record Lookup(Item item, boolean usage, int index) {}

	private static @Nullable Item lookupItem;
	private static boolean lookupUsage;
	private static int lookupIndex;
	private static final Deque<Lookup> history = new ArrayDeque<>();

	private static WeakReference<Object> attachOwner = new WeakReference<>(null);
	private static boolean refreshData;
	private static boolean wantsFocus;

	@Nullable
	private static EditBox searchBox;

	private record Cell(int x, int y, int w, int h, CellType type, @Nullable Object payload) {}

	private enum CellType { LIST_ITEM, NAV_BACK, NAV_PREV, NAV_NEXT, DETAIL_SLOT, DETAIL_RESULT }

	private static final class Metrics {
		int left;
		int top;
		int right;
		int bottom;
		int cols;
		int rows;
		List<Item> visibleItems = List.of();
		final List<Cell> cells = new ArrayList<>();
		List<AtlasEntry> detailEntries = List.of();
		@Nullable AtlasEntry detailEntry;
		boolean listMode;
	}

	private static @Nullable Metrics metrics;

	public static void attach(final Object screen) {
		if (attachOwner.get() != screen) {
			refreshData = true;
			wantsFocus = true;
			attachOwner = new WeakReference<>(screen);
		}
	}

	public static void setSearch(final EditBox box) {
		searchBox = box;
	}

	public static boolean contains(final CharmDeckScreen screen, final double mouseX, final double mouseY) {
		if (screen.isBookOpen()) {
			return false;
		}
		int[] b = bounds(screen);
		return mouseX >= b[0] && mouseY >= b[1] && mouseX < b[2] && mouseY < b[3];
	}

	public static @Nullable int[] stripBounds(final CharmDeckScreen screen) {
		return screen.isBookOpen() ? null : bounds(screen);
	}

	public static boolean handleClick(final CharmDeckScreen screen, final double mouseX, final double mouseY,
	                                  final int button) {
		Metrics m = metrics;
		if (m == null) {
			return false;
		}
		int mx = (int) mouseX;
		int my = (int) mouseY;
		if (mx >= m.left && mx < m.right && my >= m.bottom && my < m.bottom + SEARCH_TOTAL + 4) {
			return false;
		}
		Cell hit = null;
		for (Cell cell : m.cells) {
			if (mx >= cell.x() && my >= cell.y() && mx < cell.x() + cell.w() && my < cell.y() + cell.h()) {
				hit = cell;
			}
		}
		if (hit == null) {
			return false;
		}
		Minecraft mc = Minecraft.getInstance();
		AtlasIndex index = AtlasIndex.get(mc, refreshData);
		refreshData = false;
		switch (hit.type()) {
			case NAV_BACK -> {
				navigateBack();
				return true;
			}
			case NAV_PREV -> {
				if (!m.detailEntries.isEmpty()) {
					lookupIndex = Math.floorMod(lookupIndex - 1, m.detailEntries.size());
				}
				return true;
			}
			case NAV_NEXT -> {
				if (!m.detailEntries.isEmpty()) {
					lookupIndex = Math.floorMod(lookupIndex + 1, m.detailEntries.size());
				}
				return true;
			}
			case LIST_ITEM -> {
				Item item = (Item) hit.payload();
				pushCurrent();
				lookupItem = item;
				lookupUsage = false;
				lookupIndex = 0;
				return true;
			}
			case DETAIL_SLOT -> {
				AtlasEntry entry = m.detailEntry;
				Object[] payload = (Object[]) hit.payload();
				int slotIndex = (Integer) payload[1];
				List<ItemStack> slot = slotOf(entry, slotIndex);
				if (slot.isEmpty()) {
					return true;
				}
				if (button == 1 && entry instanceof AtlasEntry.JewelingEntry jeweling) {
					if (slotIndex == 0) {
						jeweling.cycleSetting();
					} else {
						jeweling.cycleInset();
					}
					return true;
				}
				Item item = slot.getFirst().getItem();
				pushCurrent();
				lookupItem = item;
				lookupUsage = true;
				lookupIndex = 0;
				return true;
			}
			case DETAIL_RESULT -> {
				ItemStack stack = (ItemStack) hit.payload();
				pushCurrent();
				lookupItem = stack.getItem();
				lookupUsage = false;
				lookupIndex = 0;
				return true;
			}
		}
		return false;
	}

	public static boolean handleScroll(final CharmDeckScreen screen, final double mouseX, final double mouseY,
	                                   final double scrollY) {
		Metrics m = metrics;
		if (m == null || !m.listMode) {
			return false;
		}
		if (mouseX < m.left || mouseX >= m.right) {
			return false;
		}
		int dir = (int) Math.signum(-scrollY);
		if (dir == 0) {
			return false;
		}
		listRow = Math.max(0, listRow + dir);
		int totalRows = (m.visibleItems.size() + m.cols - 1) / m.cols;
		listRow = Math.min(listRow, Math.max(0, totalRows - m.rows));
		return true;
	}

	private static int[] bounds(final CharmDeckScreen screen) {
		int cols = Math.max(COLUMNS_MIN, Math.min(COLUMNS_MAX, (screen.width - 24) / CELL));
		int right = screen.width - MARGIN;
		int left = right - cols * CELL;
		int top = MARGIN;
		int bottom = screen.height - SEARCH_TOTAL - MARGIN * 2;
		return new int[]{left, top, right, bottom};
	}

	public static void extract(final CharmDeckScreen screen, final GuiGraphicsExtractor gfx,
	                           final int mouseX, final int mouseY) {
		Minecraft mc = Minecraft.getInstance();
		AtlasIndex.get(mc, refreshData);
		refreshData = false;
		syncSearchWidget(screen);

		if (screen.isBookOpen()) {
			metrics = null;
			return;
		}
		AtlasIndex index = AtlasIndex.get(mc, false);

		Metrics m = new Metrics();
		int[] b = bounds(screen);
		m.left = b[0];
		m.top = b[1];
		m.right = b[2];
		m.bottom = b[3];
		m.cols = (m.right - m.left) / CELL;
		m.rows = (m.bottom - m.top) / CELL;

		m.listMode = lookupItem == null;
		if (m.listMode) {
			drawList(gfx, m, mc);
		} else {
			drawDetail(gfx, m, mc);
		}
		hoverOverlay(gfx, m, mouseX, mouseY);
		metrics = m;
	}

	private static void syncSearchWidget(final CharmDeckScreen screen) {
		EditBox box = searchBox;
		if (box == null) {
			return;
		}
		boolean shown = !screen.isBookOpen();
		box.setVisible(shown);
		if (!shown) {
			if (box.isFocused()) {
				box.setFocused(false);
			}
			wantsFocus = false;
			return;
		}
		int[] b = bounds(screen);
		int width = b[2] - b[0];
		int searchX = b[0] + MARGIN * 2;
		int searchY = b[3] + MARGIN * 3;
		box.setX(searchX);
		box.setY(searchY);
		box.setWidth(Math.max(40, width - MARGIN * 4));
		if (wantsFocus) {
			box.setFocused(true);
			wantsFocus = false;
		}
	}

	private static List<Item> filteredItems(final AtlasIndex index, final @Nullable String filter) {
		if (filter == null || filter.isEmpty()) {
			return index.items();
		}
		List<Item> out = new ArrayList<>();
		for (Item item : index.items()) {
			if (matches(item, filter)) {
				out.add(item);
			}
		}
		return out;
	}

	private static boolean matches(final Item item, final String filter) {
		ItemStack stack = new ItemStack(item);
		if (stack.getHoverName().getString().toLowerCase(Locale.ROOT).contains(filter)) {
			return true;
		}
		return net.minecraft.core.registries.BuiltInRegistries.ITEM
				.getKey(item).getPath().toLowerCase(Locale.ROOT).contains(filter);
	}

	private static void drawList(final GuiGraphicsExtractor gfx, final Metrics m, final Minecraft mc) {
		AtlasIndex index = AtlasIndex.get(mc, false);
		String filter = searchBox == null ? null : searchBox.getValue().strip().toLowerCase(Locale.ROOT);
		m.visibleItems = filteredItems(index, filter);
		if (m.visibleItems.isEmpty()) {
			drawDim(gfx, mc, Component.translatable("gui.lamplight.atlas.no_match"),
					m.left + 4, m.top + 6, TEXT_DIM);
			return;
		}
		int totalRows = (m.visibleItems.size() + m.cols - 1) / m.cols;
		listRow = Math.min(listRow, Math.max(0, totalRows - m.rows));
		for (int offset = 0; offset < m.visibleItems.size(); offset++) {
			int row = offset / m.cols - listRow;
			int col = offset % m.cols;
			if (row < 0 || row >= m.rows) {
				continue;
			}
			Item item = m.visibleItems.get(offset);
			int x = m.left + col * CELL;
			int y = m.top + row * CELL;
			ItemStack preview = previewStack(index, item);
			if (!preview.isEmpty()) {
				gfx.item(preview, x + 2, y + 2);
			}
			m.cells.add(new Cell(x, y, CELL, CELL, CellType.LIST_ITEM, item));
		}
	}

	private static void drawDetail(final GuiGraphicsExtractor gfx, final Metrics m, final Minecraft mc) {
		AtlasIndex index = AtlasIndex.get(mc, false);
		List<AtlasEntry> entries = lookupUsage
				? index.usagesFor(lookupItem)
				: index.resultsFor(lookupItem);
		m.detailEntries = entries;
		lookupIndex = entries.isEmpty() ? 0 : Math.floorMod(lookupIndex, entries.size());

		int navY = m.top;
		int backW = 16;
		drawGlyph(gfx, mc, "<", m.left, navY, TEXT_MAIN);
		m.cells.add(new Cell(m.left - 2, navY - 2, backW, 14, CellType.NAV_BACK, null));

		if (!entries.isEmpty()) {
			String count = (lookupIndex + 1) + "/" + entries.size();
			int nextX = m.right - 10;
			int prevX = nextX - 16;
			int countX = prevX - 6 - mc.font.width(count);
			drawGlyph(gfx, mc, "<", prevX, navY, TEXT_MAIN);
			drawGlyph(gfx, mc, ">", nextX, navY, TEXT_MAIN);
			drawDim(gfx, mc, Component.literal(count), countX, navY, TEXT_DIM);
			m.cells.add(new Cell(prevX - 2, navY - 2, 14, 14, CellType.NAV_PREV, null));
			m.cells.add(new Cell(nextX - 2, navY - 2, 14, 14, CellType.NAV_NEXT, null));
		}

		Component title = Component.translatable(lookupUsage ? "gui.lamplight.atlas.usages_title"
				: "gui.lamplight.atlas.recipes_title", new ItemStack(lookupItem).getHoverName());
		drawGlyph(gfx, mc, title, m.left, navY + 12, TEXT_CARD);

		int contentY = m.top + 26;
		if (entries.isEmpty()) {
			drawDim(gfx, mc, Component.translatable("gui.lamplight.atlas.empty"),
					m.left + 2, contentY + 4, TEXT_DIM);
			m.detailEntry = null;
			return;
		}
		AtlasEntry entry = entries.get(lookupIndex);
		m.detailEntry = entry;
		Player player = mc.player;

		int slotX = m.left;
		int slotY = contentY + CELL;
		switch (entry.kind()) {
			case SHAPED -> {
				AtlasEntry.RecipeEntry recipe = (AtlasEntry.RecipeEntry) entry;
				for (int i = 0; i < recipe.ingredientSlots().size(); i++) {
					int row = i / Math.max(1, recipe.gridWidth());
					int col = i % Math.max(1, recipe.gridWidth());
					placeSlot(gfx, m, recipe.ingredientSlots().get(i), slotX + col * CELL, slotY + row * CELL, i);
				}
			}
			case SHAPELESS, OTHER -> {
				List<List<ItemStack>> slots = entry.ingredientSlots();
				for (int i = 0; i < Math.min(9, slots.size()); i++) {
					int row = i / 3;
					int col = i % 3;
					placeSlot(gfx, m, slots.get(i), slotX + col * CELL, slotY + row * CELL, i);
				}
				if (slots.size() > 9) {
					drawDim(gfx, mc, Component.literal("+" + (slots.size() - 9)), slotX + 3 * CELL, slotY + 2 * CELL + 4, TEXT_DIM);
				}
			}
			case FURNACE -> {
				List<List<ItemStack>> slots = entry.ingredientSlots();
				placeSlot(gfx, m, slots.get(0), slotX, slotY, 0);
				placeSlot(gfx, m, slots.get(1), slotX, slotY + CELL + 2, 1);
			}
			case SMITHING, STONECUTTER -> {
				List<List<ItemStack>> slots = entry.ingredientSlots();
				for (int i = 0; i < slots.size(); i++) {
					placeSlot(gfx, m, slots.get(i), slotX + i * CELL, slotY, i);
				}
			}
			case JEWELING -> {
				AtlasEntry.JewelingEntry jeweling = (AtlasEntry.JewelingEntry) entry;
				List<List<ItemStack>> slots = jeweling.ingredientSlots();
				for (int i = 0; i < slots.size(); i++) {
					int row = i / 4;
					int col = i % 4;
					placeSlot(gfx, m, slots.get(i), slotX + col * CELL, slotY + row * CELL, i);
				}
			}
		}

		int layoutWidth = Math.max(1, layoutWidth(m));
		int resultX = m.left + layoutWidth + CELL;
		int arrowX = m.left + layoutWidth + 4;
		List<ItemStack> results = entry.resultStacks();
		ItemStack shown = results.isEmpty() ? ItemStack.EMPTY : results.getFirst();
		if (!shown.isEmpty()) {
			gfx.item(shown, resultX + 2, slotY + 2);
		}
		if (entry instanceof AtlasEntry.JewelingEntry jeweling && player != null && !jeweling.available(player)) {
			gfx.fill(resultX, slotY, resultX + CELL - 4, slotY + CELL - 4, LOCKED);
		}
		m.cells.add(new Cell(resultX, slotY, CELL - 4, CELL - 4, CellType.DETAIL_RESULT, shown));
		gfx.text(mc.font, Component.literal("\u25b6"), arrowX, slotY + 4, TEXT_DIM, false);

		if (entry instanceof AtlasEntry.JewelingEntry jeweling) {
			CharmTemplate def = jeweling.def();
			int lineY = m.top + 26 + jeweling.insetRowSpan() * CELL + 6;
			gfx.text(mc.font, Component.translatable("charm_slot_type.lamplight." + def.slotType())
					.withStyle(ChatFormatting.GRAY), m.left, lineY, TEXT_MAIN, false);
			if (def.focus() != 1.0D) {
				gfx.text(mc.font, "focus x" + String.format(Locale.ROOT, "%.2f", def.focus()),
						m.left, lineY + 10, TEXT_DIM, false);
			}
			if (player != null && !jeweling.available(player)) {
				Item mold = jeweling.moldItem();
				Component moldName = mold == null ? Component.literal(def.item().toString())
						: new ItemStack(mold).getHoverName();
				drawDim(gfx, mc, Component.translatable("gui.lamplight.atlas.mold_required", moldName),
						m.left, lineY + (def.focus() != 1.0D ? 20 : 10), TEXT_DIM);
			}
		} else {
			@Nullable ItemStack station = entry.stationIcon();
			if (station != null) {
				int lineY = m.bottom - 12;
				drawDim(gfx, mc, Component.translatable("gui.lamplight.atlas.station_at", station.getHoverName()),
						m.left, lineY, TEXT_DIM);
			}
		}
	}

	private static int layoutWidth(final Metrics m) {
		AtlasEntry entry = m.detailEntry;
		if (entry == null) {
			return 0;
		}
		int colsUsed = 0;
		switch (entry.kind()) {
			case SHAPED -> colsUsed = ((AtlasEntry.RecipeEntry) entry).gridWidth();
			case SHAPELESS, OTHER -> {
				int slots = entry.ingredientSlots().size();
				colsUsed = Math.min(3, Math.max(1, slots));
			}
			case FURNACE -> colsUsed = 1;
			case SMITHING -> colsUsed = 3;
			case STONECUTTER -> colsUsed = 1;
			case JEWELING -> colsUsed = Math.min(4, jwelingSlots(entry));
		}
		return colsUsed * CELL;
	}

	private static int jwelingSlots(final AtlasEntry entry) {
		return entry.ingredientSlots().size();
	}

	private static @Nullable Cell placeSlot(final GuiGraphicsExtractor gfx, final Metrics m,
	                                        final List<ItemStack> alternatives, final int x, final int y,
	                                        final int slotIndex) {
		if (!alternatives.isEmpty()) {
			gfx.fakeItem(alternatives.getFirst(), x, y);
		}
		Cell cell = new Cell(x, y, CELL - 4, CELL - 4, CellType.DETAIL_SLOT, new Object[]{alternatives, slotIndex});
		m.cells.add(cell);
		return cell;
	}

	private static void hoverOverlay(final GuiGraphicsExtractor gfx, final Metrics m,
	                                 final int mouseX, final int mouseY) {
		Cell found = null;
		for (Cell cell : m.cells) {
			if (mouseX >= cell.x() && mouseY >= cell.y()
					&& mouseX < cell.x() + cell.w() && mouseY < cell.y() + cell.h()) {
				found = cell;
			}
		}
		if (found != null) {
			gfx.fill(found.x(), found.y(), found.x() + found.w(), found.y() + found.h(), HOVER);
		}
	}

	private static void drawGlyph(final GuiGraphicsExtractor gfx, final Minecraft mc, final String text,
	                              final int x, final int y, final int color) {
		gfx.text(mc.font, text, x, y, color, false);
	}

	private static void drawGlyph(final GuiGraphicsExtractor gfx, final Minecraft mc, final Component text,
	                              final int x, final int y, final int color) {
		gfx.text(mc.font, text, x, y, color, false);
	}

	private static void drawDim(final GuiGraphicsExtractor gfx, final Minecraft mc, final Component text,
	                            final int x, final int y, final int color) {
		gfx.text(mc.font, text, x, y, color, false);
	}

	private static ItemStack previewStack(final AtlasIndex index, final Item item) {
		List<AtlasEntry> entries = index.resultsFor(item);
		if (entries.isEmpty()) {
			return ItemStack.EMPTY;
		}
		List<ItemStack> results = entries.getFirst().resultStacks();
		return results.isEmpty() ? ItemStack.EMPTY : results.getFirst();
	}

	private static List<ItemStack> slotOf(final @Nullable AtlasEntry entry, final int slotIndex) {
		if (entry == null || entry.ingredientSlots().size() <= slotIndex) {
			return List.of();
		}
		return entry.ingredientSlots().get(slotIndex);
	}

	private static void pushCurrent() {
		if (lookupItem != null) {
			history.push(new Lookup(lookupItem, lookupUsage, lookupIndex));
		}
	}

	private static void navigateBack() {
		Lookup popped = history.poll();
		if (popped == null) {
			lookupItem = null;
			return;
		}
		lookupItem = popped.item();
		lookupUsage = popped.usage();
		lookupIndex = popped.index();
	}

	public static @Nullable List<Component> hoverLines(final Minecraft mc, final double mouseX, final double mouseY) {
		Metrics m = metrics;
		if (m == null) {
			return null;
		}
		int mx = (int) mouseX;
		int my = (int) mouseY;
		Cell hit = null;
		for (Cell cell : m.cells) {
			if (mx >= cell.x() && my >= cell.y() && mx < cell.x() + cell.w() && my < cell.y() + cell.h()) {
				hit = cell;
			}
		}
		if (hit == null) {
			return null;
		}
		return switch (hit.type()) {
			case LIST_ITEM -> itemLines(mc, previewStack(AtlasIndex.get(mc, false), (Item) hit.payload()),
					"gui.lamplight.atlas.click_recipes");
			case NAV_BACK, NAV_PREV, NAV_NEXT -> navLines(hit.type());
			case DETAIL_SLOT -> detailSlotLines(mc, m, hit);
			case DETAIL_RESULT -> itemLines(mc, (ItemStack) hit.payload(), "gui.lamplight.atlas.click_recipes");
		};
	}

	private static List<Component> navLines(final CellType type) {
		String key = switch (type) {
			case NAV_BACK -> "gui.lamplight.atlas.back";
			case NAV_PREV -> "gui.lamplight.atlas.previous_page";
			case NAV_NEXT -> "gui.lamplight.atlas.next_page";
			default -> "";
		};
		return List.of(Component.translatable(key));
	}

	private static List<Component> itemLines(final Minecraft mc, final ItemStack stack, final String hintKey) {
		if (stack == null || stack.isEmpty()) {
			return List.of();
		}
		Item.TooltipContext context = Item.TooltipContext.of(mc.level);
		List<Component> lines = new ArrayList<>(stack.getTooltipLines(context, mc.player, TooltipFlag.NORMAL));
		lines.add(Component.translatable(hintKey).withStyle(ChatFormatting.DARK_GRAY));
		return lines;
	}

	@SuppressWarnings ("all")
	private static List<Component> detailSlotLines(final Minecraft mc, final Metrics m, final Cell hit) {
		AtlasEntry entry = m.detailEntry;
		Object[] payload = (Object[]) hit.payload();
		List<ItemStack> alternatives = (List<ItemStack>) payload[0];
		int slotIndex = (Integer) payload[1];
		List<Component> lines = new ArrayList<>();
		if (!alternatives.isEmpty()) {
			ItemStack stack = alternatives.getFirst();
			lines.addAll(stack.getTooltipLines(Item.TooltipContext.of(mc.level), mc.player, TooltipFlag.NORMAL));
			if (alternatives.size() > 1) {
				lines.add(Component.translatable("gui.lamplight.atlas.alternatives", alternatives.size())
						.withStyle(ChatFormatting.DARK_GRAY));
			}
		}
		lines.add(Component.translatable("gui.lamplight.atlas.click_usages").withStyle(ChatFormatting.DARK_GRAY));
		if (entry instanceof AtlasEntry.JewelingEntry jeweling) {
			int cost = slotIndex == 0 ? jeweling.settingCost() : jeweling.insetCost();
			lines.add(Component.literal("\u00d7" + cost).withStyle(ChatFormatting.DARK_GRAY));
			lines.add(Component.translatable("gui.lamplight.atlas.cycle").withStyle(ChatFormatting.DARK_GRAY));
		}
		return lines;
	}
}
