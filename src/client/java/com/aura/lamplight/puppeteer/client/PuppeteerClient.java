package com.aura.lamplight.puppeteer.client;

import net.fabricmc.api.ClientModInitializer;

public class PuppeteerClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        AnimationClientState.initialize();
    }
}
