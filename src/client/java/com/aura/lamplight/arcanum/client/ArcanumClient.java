package com.aura.lamplight.arcanum.client;

import com.aura.lamplight.arcanum.client.ability.Aerodynamics;
import com.aura.lamplight.arcanum.client.ability.Dash;
import com.aura.lamplight.arcanum.client.ability.DoubleJump;
import com.aura.lamplight.arcanum.client.ability.FlowGlide;
import com.aura.lamplight.arcanum.client.ability.JetGlide;
import com.aura.lamplight.arcanum.client.ability.WaveDash;
import com.aura.lamplight.arcanum.network.ArcanumPackets;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class ArcanumClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {

		ClientTickEvents.END_CLIENT_TICK.register(Aerodynamics::tick);
		ClientTickEvents.END_CLIENT_TICK.register(Dash::tick);
		ClientTickEvents.END_CLIENT_TICK.register(DoubleJump::tick);
		ClientTickEvents.END_CLIENT_TICK.register(WaveDash::tick);
		ClientTickEvents.END_CLIENT_TICK.register(FlowGlide::tick);
		ClientTickEvents.END_CLIENT_TICK.register(JetGlide::tick);

		ClientPlayNetworking.registerGlobalReceiver(ArcanumPackets.JetBoostPayload.TYPE, (payload, context) -> {
			context.client().execute(() -> JetGlide.setBoost(payload.ticks()));
		});
	}
}
