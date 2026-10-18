package com.aura.lamplight.armory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Armory implements ModInitializer {
	public static final String MOD_ID = "lamplight";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// force attachment registration
		Object ignored = BackSlotAttachments.BACK_STACK;

		ModItems.register();
		PayloadTypeRegistry.serverboundPlay().register(DrawSheathPayload.TYPE, DrawSheathPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(DrawSheathPayload.TYPE, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayer player = context.player();
				if (player.isSpectator()) {
					return;
				}
				if (payload.hold()) {
					if (BackSlotAttachments.getBackStack(player).isEmpty()) {
						return;
					}
					BackSlotAttachments.setHolding(player, true);
				} else {
					BackSlotAttachments.setHolding(player, false);
				}
			});
		});

		LOGGER.info("Arsenal loaded.");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
