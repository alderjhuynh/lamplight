package com.aura.lamplight.puppeteer;

import com.aura.lamplight.puppeteer.animation.AnimationDefinitions;
import com.aura.lamplight.puppeteer.network.AnimationStatePayload;
import com.aura.lamplight.puppeteer.trigger.TriggerConditionTypes;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Puppeteer implements ModInitializer {
    public static final String MOD_ID = "lamplight";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Puppeteer");
        TriggerConditionTypes.initialize();
        AnimationDefinitions.initialize();

        PayloadTypeRegistry.clientboundPlay().register(AnimationStatePayload.TYPE, AnimationStatePayload.STREAM_CODEC);
        PlayerAnimationTracker.initialize();
    }

    public static Identifier id(final String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}

