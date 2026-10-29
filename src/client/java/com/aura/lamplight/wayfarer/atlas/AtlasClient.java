package com.aura.lamplight.wayfarer.atlas;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.item.crafting.display.RecipeDisplay;

import java.util.List;

public final class AtlasClient implements ClientModInitializer {

	private static volatile List<RecipeDisplay> synced = List.of();
	private static volatile int revision;

	public static List<RecipeDisplay> synced() {
		return synced;
	}

	public static int revision() {
		return revision;
	}

	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(AtlasSyncPayload.TYPE, (payload, context) -> {
			context.client().execute(() -> {
				synced = payload.displays();
				revision++;
				AtlasIndex.invalidate();
			});
		});
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			synced = List.of();
			revision++;
			AtlasIndex.invalidate();
		});
	}
}
