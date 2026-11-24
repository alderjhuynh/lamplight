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
	@SuppressWarnings("all")
	public void onInitialize() {
		// force attachment registration
		Object ignored = BackSlotAttachments.BACK_STACK;
		Object ignored2 = ArmorVisibility.VISIBILITY;

		ModDataComponents.initialize();
		ArmorySounds.initialize();
		ArmoryEntityTypes.initialize();
		TwoHanded.register();
		DualWielded.register();
		ModItems.register();
		RapierCombat.register();
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

		PayloadTypeRegistry.serverboundPlay().register(ArmorVisibilityTogglePayload.TYPE, ArmorVisibilityTogglePayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(ArmorVisibilityTogglePayload.TYPE, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayer player = context.player();
				if (player.isSpectator()) {
					return;
				}
				int bit = payload.bit();
				if (Integer.bitCount(bit) != 1 || (bit & ArmorVisibility.ALL) == 0 || (bit & ~ArmorVisibility.ALL) != 0) {
					return;
				}
				ArmorVisibility.toggle(player, bit);
			});
		});

		LOGGER.info("Armory loaded.");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
