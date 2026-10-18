package com.aura.lamplight.picturesque.client;

import com.aura.lamplight.client.LamplightKeyCategories;
import com.aura.lamplight.picturesque.client.raytrace.RaytracePostTuner;
import com.aura.lamplight.picturesque.client.raytrace.RaytraceState;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class PicturesqueClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        PicturesqueConfig config = PicturesqueConfig.load();
        PicturesqueState.get().attach(config);

        KeyMapping.Category category = LamplightKeyCategories.MAIN;
        KeyMapping toggle = KeyMappingHelper.registerKeyMapping(
                new KeyMapping("key.lamplight.toggle", InputConstants.Type.KEYSYM, InputConstants.KEY_P, category));
        KeyMapping raytrace = KeyMappingHelper.registerKeyMapping(
                new KeyMapping("key.lamplight.raytrace", InputConstants.Type.KEYSYM, InputConstants.KEY_R, category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            PicturesqueState.get().tick(client);
            RaytraceState.get().tick(client);
            RaytracePostTuner.tick(client);
            if (toggle.consumeClick() && client.player != null) {
                config.enabled = !config.enabled;
                config.save();
                client.player.sendOverlayMessage(
                        Component.translatable(config.enabled ? "message.lamplight.enabled" : "message.lamplight.disabled"));
            }
            if (raytrace.consumeClick() && client.player != null) {
                config.raytracing = !config.raytracing;
                config.save();
                client.player.sendOverlayMessage(
                        Component.translatable(config.raytracing
                                ? "message.lamplight.raytrace.enabled"
                                : "message.lamplight.raytrace.disabled"));
                RaytraceState.refreshSections(client);
            }
            if (RaytraceState.get().consumePhaseChange()
                    && config.sunRefresh && RaytraceState.get().active()) {
                RaytraceState.refreshSections(client);
            }
        });

        Minecraft.getInstance().execute(() -> com.aura.lamplight.picturesque.Picturesque.LOGGER.info(
                "Picturesque armed (grade={} @ {}, raytracing={})",
                String.format("%.2f", config.gradeStrength),
                config.enabled ? "on" : "off",
                config.raytracing ? "on" : "off"));
    }
}
