package com.aura.lamplight.folio.client;

import com.aura.lamplight.folio.item.SpellbookItem;
import com.aura.lamplight.folio.spell.Spell;
import com.aura.lamplight.folio.spell.Spells;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class SpellWheelScreen extends Screen {

    private static final int RADIUS = 58;
    private static final int TILE = 24;

    private int localSelected;
    private int lastHovered = -1;

    public SpellWheelScreen() {
        super(Component.translatable("gui.lamplight.spellbook.wheel_title"));
        this.localSelected = SpellbookClientState.selected();
    }

    @Override
    protected void init() {
        super.init();
        this.localSelected = SpellbookClientState.selected();
    }

    @Override
    public void extractRenderState(final GuiGraphicsExtractor g, final int mouseX, final int mouseY, final float partialTick) {
        super.extractRenderState(g, mouseX, mouseY, partialTick);

        int cx = this.width / 2;
        int cy = this.height / 2;
        g.fill(0, 0, this.width, this.height, 0x66000000);

        List<Identifier> ids = SpellbookClientState.spells();
        if (ids.isEmpty()) {
            g.centeredText(this.font,
                Component.translatable("gui.lamplight.spellbook.wheel_empty"), cx, cy - 6, 0xFFCCCCCC);
            g.centeredText(this.font,
                Component.translatable("gui.lamplight.spellbook.wheel_empty_hint"), cx, cy + 10, 0xFF888888);
            return;
        }

        int count = ids.size();
        int hovered = -1;
        for (int i = 0; i < count; i++) {
            double angle = (Math.PI * 2.0D * i) / count - Math.PI / 2.0D;
            int x = cx + (int) Math.round(Math.cos(angle) * RADIUS);
            int y = cy + (int) Math.round(Math.sin(angle) * RADIUS);
            boolean hover = Math.hypot(mouseX - x, mouseY - y) <= 13.0D;
            if (hover) {
                hovered = i;
            }

            int background = i == this.localSelected ? 0xDD324A6B : 0xAA16161F;
            g.fill(x - TILE / 2, y - TILE / 2, x + TILE / 2, y + TILE / 2, background);
            if (i == this.localSelected) {
                g.outline(x - TILE / 2 - 1, y - TILE / 2 - 1, TILE + 2, TILE + 2, 0xFFFFD46A);
            } else if (hover) {
                g.outline(x - TILE / 2 - 1, y - TILE / 2 - 1, TILE + 2, TILE + 2, 0xFFAAAAAA);
            }

            Spell spell = Spells.byId(ids.get(i));
            if (spell != null) {
                ItemStack icon = SpellbookItem.scrollFor(spell);
                if (!icon.isEmpty()) {
                    g.item(icon, x - 8, y - 8);
                }
            }

            int cooldown = SpellbookClientState.cooldown(ids.get(i));
            if (cooldown > 0) {
                g.fill(x - TILE / 2, y - TILE / 2, x + TILE / 2, y + TILE / 2, 0x99000000);
                g.centeredText(this.font,
                    Component.literal(String.valueOf((cooldown + 19) / 20)), x, y - 4, 0xFFFFFFFF);
            }
        }

        if (hovered >= 0 && hovered != this.lastHovered) {
            this.lastHovered = hovered;
            this.localSelected = hovered;
            SpellbookClientState.select(hovered);
        }

        Spell selected = this.selectedSpell(ids);
        if (selected != null) {
            g.centeredText(this.font, selected.name(), cx, cy - 4, 0xFFFFFFFF);
        }
        g.centeredText(this.font,
            Component.translatable("gui.lamplight.spellbook.wheel_hint"), cx, this.height - 30, 0xFFAAAAAA);
    }

    private Spell selectedSpell(final List<Identifier> ids) {
        if (ids.isEmpty()) {
            return null;
        }
        int index = Math.floorMod(this.localSelected, ids.size());
        return Spells.byId(ids.get(index));
    }

    @Override
    public boolean mouseScrolled(final double mouseX, final double mouseY, final double scrollX, final double scrollY) {
        List<Identifier> ids = SpellbookClientState.spells();
        if (!ids.isEmpty() && scrollY != 0.0D) {
            int next = Math.floorMod(this.localSelected - (int) Math.signum(scrollY), ids.size());
            this.localSelected = next;
            this.lastHovered = next;
            SpellbookClientState.select(next);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
        if (event.button() == 0 && !SpellbookClientState.spells().isEmpty()) {
            SpellbookClientState.castSelected();
            this.closeWheel();
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean keyPressed(final KeyEvent event) {
        if (FolioKeys.CAST.matches(event)) {
            SpellbookClientState.castSelected();
            this.closeWheel();
            return true;
        }
        if (FolioKeys.WHEEL.matches(event)) {
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean keyReleased(final KeyEvent event) {
        if (FolioKeys.WHEEL.matches(event)) {
            this.closeWheel();
            return true;
        }
        return super.keyReleased(event);
    }

    @Override
    public boolean mouseReleased(final MouseButtonEvent event) {
        if (FolioKeys.WHEEL.matchesMouse(event)) {
            this.closeWheel();
            return true;
        }
        return super.mouseReleased(event);
    }

    private void closeWheel() {
        while (FolioKeys.WHEEL.consumeClick()) {
        }
        while (FolioKeys.CAST.consumeClick()) {
        }
        if (this.minecraft != null) {
            this.minecraft.gui.setScreen(null);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
