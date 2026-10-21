package com.aura.lamplight.arcanum;

import com.aura.lamplight.arcanum.network.ArcanumPackets;
import com.aura.lamplight.arcanum.server.AerodynamicsHandler;
import com.aura.lamplight.arcanum.server.DashHandler;
import com.aura.lamplight.arcanum.server.DoubleJumpHandler;
import com.aura.lamplight.arcanum.server.CapstoneHandler;
import com.aura.lamplight.arcanum.server.FlowGlideHandler;
import com.aura.lamplight.arcanum.server.JetServerHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Arcanum implements ModInitializer {
	public static final String MOD_ID = "lamplight";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Arcanum initialized");

		PayloadTypeRegistry.serverboundPlay().register(ArcanumPackets.GlidePayload.TYPE, ArcanumPackets.GlidePayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(ArcanumPackets.AerodynamicsPayload.TYPE, ArcanumPackets.AerodynamicsPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(ArcanumPackets.DashPayload.TYPE, ArcanumPackets.DashPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(ArcanumPackets.DoubleJumpPayload.TYPE, ArcanumPackets.DoubleJumpPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(ArcanumPackets.FlowGlidePayload.TYPE, ArcanumPackets.FlowGlidePayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(ArcanumPackets.JetBoostPayload.TYPE, ArcanumPackets.JetBoostPayload.CODEC);

		JetServerHandler.register();
		AerodynamicsHandler.register();
		DashHandler.register();
		DoubleJumpHandler.register();
		FlowGlideHandler.register();
		CapstoneHandler.register();

		ServerTickEvents.END_SERVER_TICK.register(JetServerHandler::tick);
		ServerTickEvents.END_SERVER_TICK.register(FlowGlideHandler::tick);

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			JetServerHandler.clear(handler.getPlayer().getUUID());
			FlowGlideHandler.clear(handler.getPlayer().getUUID());
		});
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
