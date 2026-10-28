package com.aura.lamplight.wayfarer.client.waystone;

import com.aura.lamplight.wayfarer.waystone.WaystoneTravelListPayload;
import com.aura.lamplight.wayfarer.waystone.WaystoneTravelPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.Optional;

/**
 * The travel list shown after touching a lit waystone: every other kindled
 * stone in this sky, nearest first. Picking one asks the server to gather
 * the light; the ritual (and the jump) live entirely server-side.
 *
 * <p>Styled after the Waypoints screen: plain buttons over a soft darkened
 * field, wheel-scrolled rows, one selection, Travel/Done along the bottom.
 * The origin stone is hidden from the list — a stone is a door, not its own
 * destination.</p>
 *
 * <p>Each row keeps one thought to itself: the stone's name (or, for a stone
 * its wanderers have not named, its light's coordinates) centered on the
 * button, how far the walk would be resting quietly at the row's edge, and
 * the exact ground it stands on offered only when asked — held in the hover
 * tooltip.</p>
 */
public class WaystoneTravelScreen extends Screen {

	private static final int ROW_HEIGHT = 24;
	private static final int ROWS_VISIBLE = 7;
	private static final int TOP = 44;
	/** Labels keep to roughly this much room so distance labels never meet them. */
	private static final int NAME_LABEL_LIMIT = 32;

	private final BlockPos source;
	private final List<WaystoneTravelListPayload.Entry> stones;

	private WaystoneTravelListPayload.@org.jspecify.annotations.Nullable Entry selected;
	private int scroll;

	public WaystoneTravelScreen(WaystoneTravelListPayload payload) {
		super(Component.translatable("gui.lamplight.waystone.travel.title"));
		this.source = payload.source();
		this.stones = List.copyOf(payload.stones());
	}

	// ------------------------------------------------------------- layout

	@Override
	protected void init() {
		int cx = width / 2;
		int contentW = Math.min(300, width - 16);
		int contentX0 = cx - contentW / 2;

		int listBottom = Math.min(height - 66, TOP + ROWS_VISIBLE * ROW_HEIGHT);
		int rows = Math.max(1, (listBottom - TOP) / ROW_HEIGHT);
		int maxScroll = Math.max(0, this.stones.size() - rows);
		this.scroll = Mth.clamp(this.scroll, 0, maxScroll);

		if (this.stones.isEmpty()) {
			addRenderableWidget(Button.builder(
					Component.translatable("gui.lamplight.waystone.travel.empty")
							.withStyle(ChatFormatting.GRAY), b -> {})
					.bounds(contentX0, TOP, contentW, 20).build());
		} else {
			for (int i = 0; i < rows; i++) {
				int idx = this.scroll + i;
				if (idx >= this.stones.size()) {
					break;
				}
				WaystoneTravelListPayload.Entry entry = this.stones.get(idx);
				WaystoneTravelListPayload.Entry ref = entry;
				addRenderableWidget(Button.builder(this.rowName(entry), b -> {
					this.selected = ref.equals(this.selected) ? null : ref;
					rebuildWidgets();
				}).bounds(contentX0, TOP + i * ROW_HEIGHT, contentW, 20).build());
			}
		}

		int by = height - 38;
		int halfW = (contentW - 4) / 2;
		Button travel = Button.builder(Component.translatable("gui.lamplight.waystone.travel.travel"), b -> {
			if (this.selected != null) {
				ClientPlayNetworking.send(new WaystoneTravelPayload(this.selected.dimension(), this.selected.pos()));
				this.onClose();
			}
		}).bounds(contentX0, by, halfW, 20).build();
		travel.active = this.selected != null;
		addRenderableWidget(travel);
		addRenderableWidget(Button.builder(Component.translatable("gui.lamplight.waystone.travel.done"), b -> this.onClose())
				.bounds(contentX0 + halfW + 4, by, halfW, 20).build());
	}

	private Component rowName(WaystoneTravelListPayload.Entry entry) {
		String label = entry.named() ? entry.name() : coords(entry.pos());
		if (label.length() > NAME_LABEL_LIMIT) {
			label = label.substring(0, NAME_LABEL_LIMIT - 1) + "…";
		}
		return Component.literal(label);
	}

	private static String coords(BlockPos pos) {
		return pos.getX() + " " + pos.getY() + " " + pos.getZ();
	}

	private static int distance(BlockPos pos, BlockPos source) {
		double dx = pos.getX() + 0.5 - (source.getX() + 0.5);
		double dy = pos.getY() + 1.5 - (source.getY() + 1.5);
		double dz = pos.getZ() + 0.5 - (source.getZ() + 0.5);
		return Mth.floor(Math.sqrt(dx * dx + dy * dy + dz * dz));
	}

	private void drawSelectionOutline(GuiGraphicsExtractor gfx) {
		int cx = width / 2;
		int contentW = Math.min(300, width - 16);
		int contentX0 = cx - contentW / 2;
		int listBottom = Math.min(height - 66, TOP + ROWS_VISIBLE * ROW_HEIGHT);
		int rows = Math.max(1, (listBottom - TOP) / ROW_HEIGHT);
		for (int i = 0; i < rows; i++) {
			int idx = this.scroll + i;
			if (idx >= this.stones.size()) {
				break;
			}
			if (this.stones.get(idx).equals(this.selected)) {
				int y = TOP + i * ROW_HEIGHT;
				gfx.outline(contentX0, y, contentW, 20, 0xFFFFFFFF);
			}
		}
	}

	/**
	 * Distances rest at the row's edge in quieter ink, so the eye reads names
	 * first and journeys second.
	 */
	private void drawDistances(GuiGraphicsExtractor gfx) {
		var font = Minecraft.getInstance().font;
		int cx = width / 2;
		int contentW = Math.min(300, width - 16);
		int contentX0 = cx - contentW / 2;
		int listBottom = Math.min(height - 66, TOP + ROWS_VISIBLE * ROW_HEIGHT);
		int rows = Math.max(1, (listBottom - TOP) / ROW_HEIGHT);
		int right = contentX0 + contentW - 6;
		for (int i = 0; i < rows; i++) {
			int idx = this.scroll + i;
			if (idx >= this.stones.size()) {
				break;
			}
			WaystoneTravelListPayload.Entry entry = this.stones.get(idx);
			Component label = Component.translatable("gui.lamplight.waystone.travel.paces",
					distance(entry.pos(), this.source));
			boolean selected = entry.equals(this.selected);
			int color = selected ? 0xFFFFFFFF : 0xFF8E8672;
			gfx.text(font, label, right - font.width(label), TOP + i * ROW_HEIGHT + 6, color, false);
		}
	}

	// ------------------------------------------------------------- input

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (scrollY < 0) this.scroll++;
		else if (scrollY > 0) this.scroll = Math.max(0, this.scroll - 1);
		rebuildWidgets();
		return true;
	}

	// ------------------------------------------------------------- render

	@Override
	public void extractBackground(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float delta) {
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float delta) {
		gfx.fill(0, 30, width, height - 30, 0x99000000);
		super.extractRenderState(gfx, mouseX, mouseY, delta);
		var font = Minecraft.getInstance().font;

		gfx.centeredText(font, this.title, width / 2, 14, 0xFFFFFFFF);

		this.drawSelectionOutline(gfx);
		this.drawDistances(gfx);
		this.offerRowDetails(gfx, mouseX, mouseY);
	}

	/**
	 * Hovering asks where a stone truly stands; the answer is offered only to
	 * those who linger, not preached to the room.
	 */
	private void offerRowDetails(GuiGraphicsExtractor gfx, int mouseX, int mouseY) {
		int cx = width / 2;
		int contentW = Math.min(300, width - 16);
		int contentX0 = cx - contentW / 2;
		int listBottom = Math.min(height - 66, TOP + ROWS_VISIBLE * ROW_HEIGHT);
		int rows = Math.max(1, (listBottom - TOP) / ROW_HEIGHT);
		if (mouseX < contentX0 || mouseX > contentX0 + contentW) {
			return;
		}
		for (int i = 0; i < rows; i++) {
			int idx = this.scroll + i;
			if (idx >= this.stones.size()) {
				break;
			}
			int y = TOP + i * ROW_HEIGHT;
			if (mouseY < y || mouseY >= y + 20) {
				continue;
			}
			WaystoneTravelListPayload.Entry entry = this.stones.get(idx);
			int meters = distance(entry.pos(), this.source);
			// three lines when named, two when not — the data speaks for itself
			List<Component> lines = entry.named()
					? List.of(
							Component.literal(entry.name()),
							Component.literal(coords(entry.pos())).withStyle(ChatFormatting.GRAY),
							Component.translatable("gui.lamplight.waystone.travel.paces", meters)
									.withStyle(ChatFormatting.GRAY))
					: List.of(
							Component.literal(coords(entry.pos())),
							Component.translatable("gui.lamplight.waystone.travel.paces", meters)
									.withStyle(ChatFormatting.GRAY));
			gfx.setTooltipForNextFrame(font, lines, Optional.empty(), mouseX, mouseY);
			return;
		}
	}

	@Override
	public void onClose() {
		if (this.minecraft != null) {
			this.minecraft.setScreenAndShow(null);
		} else {
			super.onClose();
		}
	}
}
