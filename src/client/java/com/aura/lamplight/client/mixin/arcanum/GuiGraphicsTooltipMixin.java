package com.aura.lamplight.client.mixin.arcanum;

import com.aura.lamplight.arcanum.client.tooltip.BookSlotTooltipComponent;
import com.aura.lamplight.arcanum.tooltip.TooltipBookStorage;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;

import java.util.List;

@Mixin(GuiGraphicsExtractor.class)
public class GuiGraphicsTooltipMixin {

    @WrapOperation(method = "setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/resources/Identifier;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;setTooltipForNextFrameInternal(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;Lnet/minecraft/resources/Identifier;Z)V"))
    private void arcanum$replaceBookPlaceholders(GuiGraphicsExtractor instance, Font font, List<ClientTooltipComponent> clientComponents, int x, int y, ClientTooltipPositioner positioner, Identifier style, boolean flag,
                                                  Operation<Void> original,
                                                  @Local(argsOnly = true) List<Component> components) {

        for (int i = 0; i < components.size() && i < clientComponents.size(); i++) {
            Component comp = components.get(i);
            String s = comp.getString();
            if (s.contains("__BOOK_") || s.contains("__EMPTY_")) {
                String placeholder = s.trim();
                String key = placeholder;
                if (!TooltipBookStorage.PLACEHOLDER_TO_BOOK.containsKey(key)) {
                    for (String k : TooltipBookStorage.PLACEHOLDER_TO_BOOK.keySet()) {
                        if (s.contains(k)) {
                            key = k;
                            break;
                        }
                    }
                }
                var book = TooltipBookStorage.PLACEHOLDER_TO_BOOK.get(key);
                var text = TooltipBookStorage.PLACEHOLDER_TO_TEXT.get(key);
                if (book != null && text != null) {
                    clientComponents.set(i, new BookSlotTooltipComponent(book, text));
                } else if (s.contains("__EMPTY_")) {

                    for (String k : TooltipBookStorage.PLACEHOLDER_TO_TEXT.keySet()) {
                        if (s.contains(k)) {
                            var b2 = TooltipBookStorage.PLACEHOLDER_TO_BOOK.get(k);
                            var t2 = TooltipBookStorage.PLACEHOLDER_TO_TEXT.get(k);
                            if (t2 != null) {
                                clientComponents.set(i, new BookSlotTooltipComponent(b2 != null ? b2 : ItemStack.EMPTY, t2));
                                break;
                            }
                        }
                    }
                }
            }
        }
        original.call(instance, font, clientComponents, x, y, positioner, style, flag);
    }
}
