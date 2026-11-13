package com.aura.lamplight.wayfarer.client;

import com.aura.lamplight.armory.BackSlotAttachments;
import com.aura.lamplight.tinkerer.client.CharmDeckScreen;
import com.aura.lamplight.wayfarer.BackpackAddonSlot;
import com.aura.lamplight.wayfarer.ModItems;
import com.aura.lamplight.wayfarer.Wayfarer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class BackpackAddonPanel {
	private BackpackAddonPanel() {}

	private static final Identifier PANEL_TEXTURE = Wayfarer.id("textures/gui/leather_backpack_addon.png");
	private static final int TEXTURE_WIDTH = 256;
	private static final int TEXTURE_HEIGHT = 256;

	private static boolean equipped(CharmDeckScreen screen) {
		ItemStack back = BackSlotAttachments.getBackStack(Minecraft.getInstance().player);
		return back.is(ModItems.LEATHER_BACKPACK);
	}

	public static boolean contains(CharmDeckScreen screen, double mouseX, double mouseY) {
		return visible(screen)
				&& mouseX >= screen.guiLeft() + BackpackAddonSlot.ORIGIN_X
				&& mouseX < screen.guiLeft() + BackpackAddonSlot.ORIGIN_X + BackpackAddonSlot.PANEL_WIDTH
				&& mouseY >= screen.guiTop() + BackpackAddonSlot.ORIGIN_Y
				&& mouseY < screen.guiTop() + BackpackAddonSlot.ORIGIN_Y + BackpackAddonSlot.PANEL_HEIGHT;
	}

	private static boolean visible(CharmDeckScreen screen) {
		return equipped(screen) && !screen.isBookOpen();
	}

	public static void extract(CharmDeckScreen screen, GuiGraphicsExtractor gfx) {
		if (!visible(screen)) {
			return;
		}
		gfx.blit(RenderPipelines.GUI_TEXTURED, PANEL_TEXTURE,
				screen.guiLeft() + BackpackAddonSlot.ORIGIN_X,
				screen.guiTop() + BackpackAddonSlot.ORIGIN_Y,
				0.0F, 0.0F, BackpackAddonSlot.PANEL_WIDTH, BackpackAddonSlot.PANEL_HEIGHT,
				TEXTURE_WIDTH, TEXTURE_HEIGHT);
	}

	public static void syncVisibility(CharmDeckScreen screen) {
		boolean visible = !screen.isBookOpen();
		for (Slot slot : screen.getMenu().slots) {
			if (slot instanceof BackpackAddonSlot addonSlot) {
				addonSlot.setPaneVisible(visible);
			}
		}
	}
}
