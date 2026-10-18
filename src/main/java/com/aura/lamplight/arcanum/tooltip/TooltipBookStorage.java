package com.aura.lamplight.arcanum.tooltip;

import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public final class TooltipBookStorage {
    private TooltipBookStorage() {}

    public static final Map<String, ItemStack> PLACEHOLDER_TO_BOOK = new HashMap<>();

    public static final Map<String, net.minecraft.network.chat.Component> PLACEHOLDER_TO_TEXT = new HashMap<>();

    public static void clear() {
        PLACEHOLDER_TO_BOOK.clear();
        PLACEHOLDER_TO_TEXT.clear();
    }

    public static void put(String placeholder, ItemStack book, net.minecraft.network.chat.Component text) {
        PLACEHOLDER_TO_BOOK.put(placeholder, book);
        PLACEHOLDER_TO_TEXT.put(placeholder, text);
    }
}
