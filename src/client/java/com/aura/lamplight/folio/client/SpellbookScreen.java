package com.aura.lamplight.folio.client;

import com.aura.lamplight.folio.Folio;
import com.aura.lamplight.folio.SelectSpellPayload;
import com.aura.lamplight.folio.menu.SpellbookMenu;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class SpellbookScreen extends AbstractContainerScreen<SpellbookMenu> {

    private static final int PANEL_X = 36;
    private static final int PANEL_Y = 14;
    private static final int PANEL_WIDTH = 104;
    private static final int PANEL_HEIGHT = 60;
    private static final int SLOT_SELECTED_SIZE = 18;
    private static final Identifier PANEL_TEXTURE = Folio.id("textures/gui/spellbook_panel.png");
    private static final Identifier SLOT_SELECTED_TEXTURE = Folio.id("textures/gui/spellbook_slot_selected.png");

    public SpellbookScreen(final SpellbookMenu menu, final Inventory inventory, final Component title) {
        super(menu, inventory, title, 176, 166);
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelY = 72;
    }

    @Override
    public void extractContents(final GuiGraphicsExtractor gfx, final int mouseX, final int mouseY, final float partialTick) {
        gfx.blit(RenderPipelines.GUI_TEXTURED, PANEL_TEXTURE,
            this.leftPos + PANEL_X, this.topPos + PANEL_Y, 0.0F, 0.0F,
            PANEL_WIDTH, PANEL_HEIGHT, PANEL_WIDTH, PANEL_HEIGHT);

        int selected = this.menu.selected();
        for (int i = 0; i < this.menu.spellSlots(); i++) {
            if (i == selected) {
                int x = this.leftPos + SpellbookMenu.spellSlotX(i);
                int y = this.topPos + SpellbookMenu.spellSlotY(i);
                gfx.blit(RenderPipelines.GUI_TEXTURED, SLOT_SELECTED_TEXTURE,
                    x - 1, y - 1, 0.0F, 0.0F,
                    SLOT_SELECTED_SIZE, SLOT_SELECTED_SIZE, SLOT_SELECTED_SIZE, SLOT_SELECTED_SIZE);
            }
        }

        super.extractContents(gfx, mouseX, mouseY, partialTick);

        if (!this.menu.hasSpells()) {
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
                    this.menu.select(index);
                    ClientPlayNetworking.send(new SelectSpellPayload(index));
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
