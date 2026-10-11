package com.aura.picturesque.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * Client entrypoint: loads config, caches one {@link LightMood} per tick,
 * spawns golden motes, and offers a no-GUI toggle (category + key) so
 * players can A/B the grade without leaving the world.
 *
 * <p>Lamplight tone: players learn by wandering, not reading — so there is
 * no config screen in v1, just a key that flips the whole look and a
 * persistent {@code config/picturesque.json} for pack makers.
 */
public class PicturesqueClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        PicturesqueConfig config = PicturesqueConfig.load();
        PicturesqueState.get().attach(config);

        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("picturesque", "picturesque"));
        KeyMapping toggle = KeyMappingHelper.registerKeyMapping(
                new KeyMapping("key.picturesque.toggle", InputConstants.Type.KEYSYM, InputConstants.KEY_P, category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            PicturesqueState.get().tick(client);
            MoteSpawner.tick(client);
            if (toggle.consumeClick() && client.player != null) {
                config.enabled = !config.enabled;
                config.save();
                client.player.sendOverlayMessage(
                        Component.translatable(config.enabled ? "message.picturesque.enabled" : "message.picturesque.disabled"));
            }
        });

        // Warm log line so pack startup shows the look is armed, not silent.
        Minecraft.getInstance().execute(() -> com.aura.picturesque.Picturesque.LOGGER.info(
                "Picturesque gouache grade armed (grade={}, fog={})",
                String.format("%.2f", config.gradeStrength), String.format("%.2f", config.fogStrength)));
    }
}
