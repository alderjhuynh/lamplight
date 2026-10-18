package com.aura.lamplight.arcanum.client.tooltip;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class BookSlotTooltipComponent implements ClientTooltipComponent {
    private final ItemStack book;
    private final Component enchantName;

    private final boolean isEmptySlot;

    public BookSlotTooltipComponent(ItemStack book, Component text) {
        this.book = book;
        this.isEmptySlot = book.isEmpty();
        if (isEmptySlot) {
            this.enchantName = text.copy().withStyle(ChatFormatting.DARK_GRAY);
        } else {
            this.enchantName = text.copy().withStyle(ChatFormatting.GRAY);
        }
    }

    @Override
    public int getWidth(Font font) {
        if (isEmptySlot) {
            return font.width(Component.literal("[] ").getVisualOrderText()) + font.width(enchantName.getVisualOrderText()) + 2;
        }
        int bracketLeft = font.width(Component.literal("[").getVisualOrderText());
        int bracketRight = font.width(Component.literal("] ").getVisualOrderText());
        int enchantWidth = font.width(enchantName.getVisualOrderText());
        return bracketLeft + 8 + bracketRight + enchantWidth + 2;
    }

    @Override
    public int getHeight(Font font) {
        return 12;
    }

    @Override
    public void extractText(GuiGraphicsExtractor graphics, Font font, int x, int y) {
    }

    @Override
    public void extractImage(Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
        int textY = y + (height - font.lineHeight) / 2;
        if (isEmptySlot) {
            graphics.text(font, Component.literal("[] ").withStyle(ChatFormatting.DARK_GRAY).getVisualOrderText(), x, textY, -1, true);
            int prefixWidth = font.width(Component.literal("[] ").getVisualOrderText());
            graphics.text(font, enchantName.getVisualOrderText(), x + prefixWidth, textY, -1, true);
            return;
        }
        graphics.text(font, Component.literal("[").withStyle(ChatFormatting.GRAY).getVisualOrderText(), x, textY, -1, true);
        int bracketLeftWidth = font.width(Component.literal("[").getVisualOrderText());
        int bookX = x + bracketLeftWidth + 1;
        int bookY = y + (height - 8) / 2;
        try {
            graphics.pose().pushMatrix();
            graphics.pose().translate(bookX, bookY);
            graphics.pose().scale(0.5f, 0.5f);
            graphics.item(book, 0, 0);
            graphics.pose().popMatrix();
        } catch (Exception ignored) {
            try { graphics.pose().popMatrix(); } catch (Exception e2) {}
        }
        int bracketRightX = bookX + 8 + 1;
        graphics.text(font, Component.literal("] ").withStyle(ChatFormatting.GRAY).getVisualOrderText(), bracketRightX, textY, -1, true);
        int enchantX = bracketRightX + font.width(Component.literal("] ").getVisualOrderText());
        graphics.text(font, enchantName.getVisualOrderText(), enchantX, textY, -1, true);
    }
}
