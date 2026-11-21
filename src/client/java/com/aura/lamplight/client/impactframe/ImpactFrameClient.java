package com.aura.lamplight.client.impactframe;

import com.aura.lamplight.client.LamplightKeyCategories;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;

public class ImpactFrameClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeyMapping key = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.lamplight.impact_frame", InputConstants.Type.KEYSYM, InputConstants.KEY_U,
                LamplightKeyCategories.MAIN));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (key.consumeClick()) {
                if (client.level == null) {
                    continue;
                }
                ImpactFrame.trigger();
            }
        });
    }
}
