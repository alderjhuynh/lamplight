package com.aura.picturesque.client;

import com.aura.picturesque.client.raytrace.RaytracePostTuner;
import com.aura.picturesque.client.raytrace.RaytraceState;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class PicturesqueClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        PicturesqueConfig config = PicturesqueConfig.load();
        PicturesqueState.get().attach(config);

        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("picturesque", "picturesque"));
        KeyMapping toggle = KeyMappingHelper.registerKeyMapping(
                new KeyMapping("key.picturesque.toggle", InputConstants.Type.KEYSYM, InputConstants.KEY_P, category));
        KeyMapping raytrace = KeyMappingHelper.registerKeyMapping(
                new KeyMapping("key.picturesque.raytrace", InputConstants.Type.KEYSYM, InputConstants.KEY_R, category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            PicturesqueState.get().tick(client);
            RaytraceState.get().tick(client);
            RaytracePostTuner.tick(client);
            if (toggle.consumeClick() && client.player != null) {
                config.enabled = !config.enabled;
                config.save();
                client.player.sendOverlayMessage(
                        Component.translatable(config.enabled ? "message.picturesque.enabled" : "message.picturesque.disabled"));
            }
            if (raytrace.consumeClick() && client.player != null) {
                config.raytracing = !config.raytracing;
                config.save();
                client.player.sendOverlayMessage(
                        Component.translatable(config.raytracing
                                ? "message.picturesque.raytrace.enabled"
                                : "message.picturesque.raytrace.disabled"));
                RaytraceState.refreshSections(client);
            }
            if (RaytraceState.get().consumePhaseChange()
                    && config.sunRefresh && RaytraceState.get().active()) {
                RaytraceState.refreshSections(client);
            }
        });

        Minecraft.getInstance().execute(() -> com.aura.picturesque.Picturesque.LOGGER.info(
                "Picturesque armed (grade={} @ {}, raytracing={})",
                String.format("%.2f", config.gradeStrength),
                config.enabled ? "on" : "off",
                config.raytracing ? "on" : "off"));
    }
}
