package com.aura.lamplight.armory.client;

import com.aura.lamplight.armory.ArmorVisibility;
import com.aura.lamplight.armory.ArmorVisibilityTogglePayload;
import com.aura.lamplight.client.mixin.armory.ArmorSlotAccessor;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ArmorSlot;
import net.minecraft.world.inventory.Slot;

public final class ArmorVisibilityButtons {
	private static final Identifier TEX_NORMAL = com.aura.lamplight.armory.Armory.id("textures/gui/sprites/armor_button.png");
	private static final Identifier TEX_HOVERED = com.aura.lamplight.armory.Armory.id("textures/gui/sprites/armor_button_hovered.png");
	private static final Identifier TEX_CLICKED = com.aura.lamplight.armory.Armory.id("textures/gui/sprites/armor_button_clicked.png");

	private ArmorVisibilityButtons() {
	}

	private static int leftPos(AbstractContainerScreen<?> screen) {
		try {
			java.lang.reflect.Field f = AbstractContainerScreen.class.getDeclaredField("leftPos");
			f.setAccessible(true);
			return f.getInt(screen);
		} catch (Exception e) {
			return 0;
		}
	}

	private static int topPos(AbstractContainerScreen<?> screen) {
		try {
			java.lang.reflect.Field f = AbstractContainerScreen.class.getDeclaredField("topPos");
			f.setAccessible(true);
			return f.getInt(screen);
		} catch (Exception e) {
			return 0;
		}
	}

	private static Slot unwrap(Slot slot) {
		if (slot.getClass().getSimpleName().equals("SlotWrapper")) {
			try {
				java.lang.reflect.Field f = slot.getClass().getDeclaredField("target");
				f.setAccessible(true);
				return (Slot) f.get(slot);
			} catch (Exception e) {
				return slot;
			}
		}
		return slot;
	}

	private static int bitFor(Slot slot) {
		Slot target = unwrap(slot);
		if (target instanceof ArmorSlot armorSlot) {
			net.minecraft.world.entity.EquipmentSlot es = ((ArmorSlotAccessor) (Object) armorSlot).lamplight$getEquipmentSlot();
			return ArmorVisibility.bitForEquipmentSlot(es);
		}
		if (target instanceof com.aura.lamplight.armory.BackSlot) {
			return ArmorVisibility.BACK;
		}
		return -1;
	}

	public static void extract(AbstractContainerScreen<?> screen, GuiGraphicsExtractor gfx, int mouseX, int mouseY) {
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		if (player == null || screen.getMenu() == null) {
			return;
		}
		int lp = leftPos(screen);
		int tp = topPos(screen);
		for (Slot slot : screen.getMenu().slots) {
			if (!slot.isActive()) {
				continue;
			}
			int bit = bitFor(slot);
			if (bit < 0) {
				continue;
			}
			int x = lp + slot.x + 12;
			int y = tp + slot.y - 5 ;
			boolean hovered = mouseX >= x && mouseX < x + 8 && mouseY >= y && mouseY < y + 8;
			boolean visible = ArmorVisibility.isVisibleBit(player, bit);
			Identifier tex;
			if (hovered) {
				tex = TEX_HOVERED;
			} else if (!visible) {
				tex = TEX_CLICKED;
			} else {
				tex = TEX_NORMAL;
			}
			gfx.blit(RenderPipelines.GUI_TEXTURED, tex, x, y, 0.0F, 0.0F, 8, 8, 8, 8);
		}
	}

	public static boolean mouseClicked(AbstractContainerScreen<?> screen, MouseButtonEvent event, boolean doubleClick) {
		if (event.button() != 0) {
			return false;
		}
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		if (player == null || screen.getMenu() == null) {
			return false;
		}
		int mx = (int) event.x();
		int my = (int) event.y();
		int lp = leftPos(screen);
		int tp = topPos(screen);
		for (Slot slot : screen.getMenu().slots) {
			if (!slot.isActive()) {
				continue;
			}
			int bit = bitFor(slot);
			if (bit < 0) {
				continue;
			}
			int x = lp + slot.x + 13;
			int y = tp + slot.y - 2;
			if (mx >= x && mx < x + 8 && my >= y && my < y + 8) {
				ArmorVisibility.toggle(player, bit);
				if (ClientPlayNetworking.canSend(ArmorVisibilityTogglePayload.TYPE)) {
					ClientPlayNetworking.send(new ArmorVisibilityTogglePayload(bit));
				}
				mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
				return true;
			}
		}
		return false;
	}
}
