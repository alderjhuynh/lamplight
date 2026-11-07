package com.aura.lamplight.puppeteer.fpanims;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class FpAnimsClient implements ClientModInitializer {
    private static double prevTime = (double) 0.0F;
    public static double deltaTime = (double) 0.0F;
    public static final String BUCKET_PACK_ID = "lamplight:buckets";
    public static final net.minecraft.client.renderer.state.level.CameraRenderState CAMERA_STATE = new net.minecraft.client.renderer.state.level.CameraRenderState();

    public static net.minecraft.client.renderer.state.level.CameraRenderState cameraState() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null && minecraft.gameRenderer != null) {
            return minecraft.gameRenderer.gameRenderState().levelRenderState.cameraRenderState;
        }
        return CAMERA_STATE;
    }
    public static final Logger LOGGER = LoggerFactory.getLogger("puppeteer");
    public static KeyMapping CUSTOM_KEY;

    public static void updateFrameTime() {
        double currentTime = GLFW.glfwGetTime();
        deltaTime = currentTime - prevTime;
        prevTime = currentTime;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null && minecraft.isPaused()) {
            deltaTime = (double) 0.0F;
        } else {
            deltaTime = Math.min(0.05, deltaTime);
        }
    }

    @Override
    public void onInitializeClient() {
        FpAnimsConfig.load();
        this.registerKey();
        this.addBucketPack();
        LOGGER.info("Puppeteer fp-anims initialized");
    }

    private void addBucketPack() {
        ResourceLoader.registerBuiltinPack(
            Identifier.fromNamespaceAndPath("lamplight", "buckets"),
            FabricLoader.getInstance().getModContainer("lamplight").orElseThrow(),
            net.minecraft.network.chat.Component.literal("Puppeteer 3D Buckets"),
            FpAnimsConfig.ENABLE_3D_BUCKETS
                ? net.fabricmc.fabric.api.resource.v1.pack.PackActivationType.DEFAULT_ENABLED
                : net.fabricmc.fabric.api.resource.v1.pack.PackActivationType.NORMAL
        );
    }

    private void registerKey() {
        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("lamplight", "fp_anims"));
        CUSTOM_KEY = new KeyMapping("key.lamplight.inspect", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, category);
        KeyMappingHelper.registerKeyMapping(CUSTOM_KEY);
    }
}
