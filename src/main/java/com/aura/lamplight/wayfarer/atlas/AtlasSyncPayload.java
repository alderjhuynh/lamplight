package com.aura.lamplight.wayfarer.atlas;

import com.aura.lamplight.wayfarer.Wayfarer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.display.RecipeDisplay;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings ("all")
public record AtlasSyncPayload(List<RecipeDisplay> displays) implements CustomPacketPayload {

	public static final Type<AtlasSyncPayload> TYPE = new Type<>(Wayfarer.id("atlas_sync"));

	private static final StreamCodec<RegistryFriendlyByteBuf, List<RecipeDisplay>> LIST_CODEC =
			ByteBufCodecs.collection(ArrayList::new, RecipeDisplay.STREAM_CODEC);

	public static final StreamCodec<RegistryFriendlyByteBuf, AtlasSyncPayload> CODEC =
			StreamCodec.composite(LIST_CODEC, AtlasSyncPayload::displays, AtlasSyncPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return this.TYPE;
	}

	public static List<RecipeDisplay> currentDisplays(final ServerLevel level) {
		RecipeManager manager = level.recipeAccess();
		List<RecipeDisplay> displays = new ArrayList<>();
		for (RecipeHolder<?> holder : manager.getRecipes()) {
			displays.addAll(holder.value().display());
		}
		return displays;
	}

	public static void sendTo(final ServerPlayer player) {
		if (player.level() instanceof ServerLevel level) {
			List<RecipeDisplay> displays = currentDisplays(level);
			ServerPlayNetworking.send(player, new AtlasSyncPayload(List.copyOf(displays)));
		}
	}

	public static void initialize() {
		PayloadTypeRegistry.clientboundPlay().register(TYPE, CODEC);
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
				sendTo(handler.getPlayer()));
		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
			if (success) {
				for (ServerPlayer player : server.getPlayerList().getPlayers()) {
					sendTo(player);
				}
			}
		});
	}
}
