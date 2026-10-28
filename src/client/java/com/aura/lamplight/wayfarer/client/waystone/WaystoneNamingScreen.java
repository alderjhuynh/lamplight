package com.aura.lamplight.wayfarer.client.waystone;

import com.aura.lamplight.wayfarer.waystone.WaystoneNamePayload;
import com.aura.lamplight.wayfarer.waystone.WaystoneNamingPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/**
 * The naming moments, handed over from the server: a just-kindled stone asks
 * for a name before anything else happens, and a crouch-touch on a lit stone
 * asks for a new one. Neither the whisper hint nor the buttons push text at
 * the player later — the stone simply keeps what it was given.
 */
public class WaystoneNamingScreen extends Screen {

	private static final int MAX_NAME_LENGTH = 48;

	private final BlockPos stone;
	private final String received;

	private EditBox nameBox;

	public WaystoneNamingScreen(WaystoneNamingPayload payload) {
		super(Component.translatable("gui.lamplight.waystone.name.title"));
		this.stone = payload.stone();
		this.received = payload.name();
	}

	@Override
	protected void init() {
		var font = Minecraft.getInstance().font;
		int cx = width / 2;
		int contentW = Math.min(240, width - 16);
		int x0 = cx - contentW / 2;

		int boxY = Math.max(height / 2 - 40, 80);
		this.nameBox = new EditBox(font, x0, boxY, contentW, 20,
				Component.translatable("gui.lamplight.waystone.name.title"));
		this.nameBox.setMaxLength(MAX_NAME_LENGTH);
		this.nameBox.setValue(this.received);
		this.nameBox.setResponder(value -> this.nameBox.setTextColor(
				value.isBlank() ? 0xFF707070 : 0xFFE0D6B0));
		addRenderableWidget(this.nameBox);
		this.setInitialFocus(this.nameBox);
		this.nameBox.setTextColor(this.received.isBlank() ? 0xFF707070 : 0xFFE0D6B0);

		int by = boxY + 26;
		int halfW = (contentW - 4) / 2;
		boolean renaming = !this.received.isBlank();
		addRenderableWidget(Button.builder(
				Component.translatable(renaming
						? "gui.lamplight.waystone.name.rename"
						: "gui.lamplight.waystone.name.dedicate"),
				b -> this.name()).bounds(x0, by, halfW, 20).build());
		addRenderableWidget(Button.builder(Component.translatable("gui.lamplight.waystone.name.cancel"),
				b -> this.onClose()).bounds(x0 + halfW + 4, by, halfW, 20).build());
	}

	private void name() {
		String value = this.nameBox.getValue().strip();
		ClientPlayNetworking.send(new WaystoneNamePayload(this.stone, value));
		this.onClose();
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (event.key() == GLFW.GLFW_KEY_ENTER || event.key() == GLFW.GLFW_KEY_KP_ENTER) {
			// enter: the name is kept
			this.name();
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float delta) {
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float delta) {
		gfx.fill(0, 30, width, height - 30, 0x99000000);
		super.extractRenderState(gfx, mouseX, mouseY, delta);
		var font = Minecraft.getInstance().font;

		gfx.centeredText(font, this.title, width / 2, height / 2 - 56, 0xFFFFFFFF);

		// a quiet hint that lives inside the box, not on the player's screen
		if (this.nameBox.getValue().isEmpty() && !this.nameBox.isFocused()) {
			gfx.text(font, Component.translatable("gui.lamplight.waystone.name.hint")
							.withStyle(ChatFormatting.ITALIC),
					this.nameBox.getX() + 4, this.nameBox.getY() + 6, 0xFF707070, false);
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
