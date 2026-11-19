package com.aura.lamplight.folio.client;

import com.aura.lamplight.client.LamplightKeyCategories;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class FolioKeys {
    private FolioKeys() {}

    public static final KeyMapping WHEEL = KeyMappingHelper.registerKeyMapping(
        new KeyMapping("key.lamplight.spell_wheel", GLFW.GLFW_KEY_V, LamplightKeyCategories.MAIN));

    public static final KeyMapping CAST = KeyMappingHelper.registerKeyMapping(
        new KeyMapping("key.lamplight.cast_spell", GLFW.GLFW_KEY_C, LamplightKeyCategories.MAIN));

    public static void initialize() {
    }
}
