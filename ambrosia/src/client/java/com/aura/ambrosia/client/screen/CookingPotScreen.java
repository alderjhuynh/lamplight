package com.aura.ambrosia.client.screen;

import com.aura.ambrosia.Ambrosia;
import com.aura.ambrosia.menu.CookingPotMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class CookingPotScreen extends AbstractContainerScreen<CookingPotMenu> {
    private static final Identifier TEXTURE = Ambrosia.id("textures/gui/cooking_pot.png");

    public CookingPotScreen(CookingPotMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 200, 205);
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 7;
        this.inventoryLabelY = 71;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(gfx, mouseX, mouseY, partialTick);
        gfx.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos,
                0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
        float progress = this.menu.getCookProgress();
        if (progress > 0.0F) {
            int height = Math.max(1, (int) (progress * 7.0F));
            gfx.fill(this.leftPos + 131, this.topPos + 53 - height,
                    this.leftPos + 141, this.topPos + 53, 0xFF5DAE4E);
        }
    }
}
