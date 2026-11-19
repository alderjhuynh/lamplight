package com.aura.lamplight.folio.client;

import com.aura.lamplight.folio.menu.SpellbookMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class SpellbookScreen extends AbstractContainerScreen<SpellbookMenu> {

    public SpellbookScreen(final SpellbookMenu menu, final Inventory inventory, final Component title) {
        super(menu, inventory, title, 176, 166);
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelY = 72;
    }

    @Override
    public void extractContents(final GuiGraphicsExtractor gfx, final int mouseX, final int mouseY, final float partialTick) {
        gfx.fill(this.leftPos + 36, this.topPos + 14, this.leftPos + 140, this.topPos + 74, 0x553A2A5A);

        super.extractContents(gfx, mouseX, mouseY, partialTick);

        int selected = SpellbookClientState.selected();
        for (int i = 0; i < this.menu.spellSlots(); i++) {
            if (i == selected) {
                int x = this.leftPos + SpellbookMenu.spellSlotX(i);
                int y = this.topPos + SpellbookMenu.spellSlotY(i);
                gfx.outline(x - 1, y - 1, 18, 18, 0xFFFFD46A);
            }
        }

        if (SpellbookClientState.spells().isEmpty()) {
            gfx.text(this.font,
                Component.translatable("gui.lamplight.spellbook.empty_hint"),
                this.leftPos + 42, this.topPos + 56, 0xFF8C8C8C);
        }
    }

    @Override
    public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
        if (event.button() == 0) {
            Slot slot = this.hoveredSlot;
            if (slot != null) {
                int index = this.menu.slots.indexOf(slot);
                if (index >= 0 && index < this.menu.spellSlots() && slot.hasItem()) {
                    SpellbookClientState.select(index);
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
