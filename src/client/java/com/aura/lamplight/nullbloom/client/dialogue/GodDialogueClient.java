package com.aura.lamplight.nullbloom.client.dialogue;

import com.aura.lamplight.client.LamplightKeyCategories;
import com.aura.lamplight.nullbloom.dialogue.GodDialogueCancelPayload;
import com.aura.lamplight.nullbloom.dialogue.GodDialoguePayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public class GodDialogueClient implements ClientModInitializer {
	public static final KeyMapping ADVANCE = KeyMappingHelper.registerKeyMapping(new KeyMapping(
		"key.lamplight.god_advance", InputConstants.Type.KEYSYM, InputConstants.KEY_RETURN, LamplightKeyCategories.MAIN));
	public static final KeyMapping YES = KeyMappingHelper.registerKeyMapping(new KeyMapping(
		"key.lamplight.god_yes", InputConstants.Type.KEYSYM, InputConstants.KEY_Y, LamplightKeyCategories.MAIN));
	public static final KeyMapping NO = KeyMappingHelper.registerKeyMapping(new KeyMapping(
		"key.lamplight.god_no", InputConstants.Type.KEYSYM, InputConstants.KEY_N, LamplightKeyCategories.MAIN));

	private static final GodDialogueView VIEW = new GodDialogueView();
	private boolean spaceWasDown;

	@Override
	public void onInitializeClient() {
		HudElementRegistry.attachElementAfter(VanillaHudElements.CROSSHAIR,
			Identifier.fromNamespaceAndPath("lamplight", "god_dialogue"), VIEW);

		ClientPlayNetworking.registerGlobalReceiver(GodDialoguePayload.TYPE,
			(payload, context) -> context.client().execute(() -> VIEW.begin(payload)));
		ClientPlayNetworking.registerGlobalReceiver(GodDialogueCancelPayload.TYPE,
			(payload, context) -> context.client().execute(() -> VIEW.cancel(payload.sessionId())));
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> VIEW.reset());

		ClientTickEvents.END_CLIENT_TICK.register(this::tick);
	}

	private void tick(Minecraft client) {
		boolean advance = false;
		while (ADVANCE.consumeClick()) {
			advance = true;
		}
		boolean yes = false;
		while (YES.consumeClick()) {
			yes = true;
		}
		boolean no = false;
		while (NO.consumeClick()) {
			no = true;
		}
		boolean spaceDown = client.gui.screen() == null && InputConstants.isKeyDown(client.getWindow(), InputConstants.KEY_SPACE);
		boolean spacePressed = spaceDown && !spaceWasDown;
		spaceWasDown = spaceDown;

		if (!VIEW.isActive() || client.gui.screen() != null) {
			return;
		}
		if (yes) {
			VIEW.choose(true);
		}
		if (no) {
			VIEW.choose(false);
		}
		if (advance || spacePressed) {
			VIEW.advance();
		}
	}
}
