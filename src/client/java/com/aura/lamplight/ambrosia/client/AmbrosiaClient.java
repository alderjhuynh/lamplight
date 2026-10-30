package com.aura.lamplight.ambrosia.client;

import com.aura.lamplight.ambrosia.client.appleskin.AppleSkin;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class AmbrosiaClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(AppleSkin.INSTANCE::tick);
	}
}
