package com.aura.lamplight.ambrosia.client.appleskin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.StringDecomposer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.TooltipDisplay;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Appends a food-value icon row to the tooltip of every item with a food
 * component: hunger icons on top, saturation icons below.
 * <p>
 * Hunger uses the vanilla food sprites (see {@link TextureHelper#getFoodTexture}),
 * saturation uses the 7x7 tooltip variants in the mod icon sheet
 * ({@link TextureHelper#MOD_ICONS}, rows at {@code v=27}/{@code v=34}) — the
 * same textures {@link HudOverlayHandler} draws on the HUD, just smaller.
 * <p>
 * The overlay travels inside the tooltip text lines as a
 * {@link FoodOverlayTextComponent} (a {@link Component} that is also a
 * {@link FormattedCharSequence}) and is unwrapped back into the
 * {@link FoodOverlay} image by the {@code ClientTooltipComponent} mixin.
 */
public final class TooltipOverlayHandler {
    public static final TooltipOverlayHandler INSTANCE = new TooltipOverlayHandler();

    private static final RenderPipeline GUI_TEXTURED = RenderPipelines.GUI_TEXTURED;

    private TooltipOverlayHandler() {
    }

    static abstract class EmptyText implements Component {
        @Override
        public Style getStyle() {
            return Style.EMPTY;
        }

        @Override
        public ComponentContents getContents() {
            return PlainTextContents.EMPTY;
        }

        static final List<Component> EMPTY_SIBLINGS = new ArrayList<>();

        @Override
        public List<Component> getSiblings() {
            return EMPTY_SIBLINGS;
        }
    }

    /** Binds the overlay to a text line so it survives tooltip line processing. */
    public static class FoodOverlayTextComponent extends EmptyText implements FormattedCharSequence {
        public final FoodOverlay foodOverlay;

        FoodOverlayTextComponent(FoodOverlay foodOverlay) {
            this.foodOverlay = foodOverlay;
        }

        @Override
        public FormattedCharSequence getVisualOrderText() {
            return this;
        }

        @Override
        public boolean accept(FormattedCharSink visitor) {
            return StringDecomposer.iterateFormatted(this, getStyle(), visitor);
        }
    }

    public static class FoodOverlay implements ClientTooltipComponent, TooltipComponent {
        private final int hunger;
        private final float saturation;
        private final boolean rotten;

        private final int hungerBars;
        private final String hungerBarsText;

        private final int saturationBars;
        private final String saturationBarsText;

        FoodOverlay(FoodProperties food, boolean rotten) {
            this.hunger = food.nutrition();
            this.saturation = food.saturation();
            this.rotten = rotten;

            int bars = (int) Math.ceil(Math.abs(hunger) / 2f);
            if (bars > 10) {
                hungerBarsText = "x" + (hunger < 0 ? -bars : bars);
                bars = 1;
            } else {
                hungerBarsText = null;
            }
            hungerBars = bars;

            bars = (int) Math.ceil(Math.abs(saturation) / 2f);
            if (bars > 10 || bars == 0) {
                saturationBarsText = "x" + (saturation < 0 ? -bars : bars);
                bars = 1;
            } else {
                saturationBarsText = null;
            }
            saturationBars = bars;
        }

        boolean shouldRenderHungerBars() {
            return hungerBars > 0;
        }

        @Override
        public int getHeight(Font font) {
            // hunger row + spacing + saturation row + a little breathing room
            return 9 + 1 + 7 + 3;
        }

        @Override
        public int getWidth(Font font) {
            int hungerLength = hungerBars * 9;
            if (hungerBarsText != null) {
                hungerLength += font.width(hungerBarsText);
            }
            int saturationLength = saturationBars * 7;
            if (saturationBarsText != null) {
                saturationLength += font.width(saturationBarsText);
            }
            return Math.max(hungerLength, saturationLength);
        }

        @Override
        public void extractImage(Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
            TooltipOverlayHandler.INSTANCE.onRenderTooltip(graphics, this, x, y, font);
        }
    }

    /**
     * Appends the food overlay plus any on-eat status effects to the tooltip
     * lines of any food item. Called from the {@code ItemStack.getTooltipLines}
     * mixin. Effects reuse the vanilla potion tooltip formatting so beneficial
     * effects (Famished, Nourishment) render in the blue buff style.
     */
    public void onItemTooltip(ItemStack stack, Item.TooltipContext context, TooltipFlag type, List<Component> tooltip) {
        if (stack.isEmpty() || !FoodHelper.isFood(stack)) {
            return;
        }

        // Match vanilla: hidden tooltips stay hidden outside creative.
        if (!type.isCreative()
                && stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT).hideTooltip()) {
            return;
        }

        FoodProperties food = FoodHelper.getDefaultFoodValues(stack);
        FoodOverlay overlay = new FoodOverlay(food, FoodHelper.isRotten(stack));

        try {
            if (overlay.shouldRenderHungerBars()) {
                tooltip.add(new FoodOverlayTextComponent(overlay));
            }

            List<MobEffectInstance> statusEffects = FoodHelper.getStatusEffects(stack);
            if (!statusEffects.isEmpty()) {
                float durationScale = stack.getOrDefault(DataComponents.POTION_DURATION_SCALE, 1.0F);
                PotionContents.addPotionTooltip(statusEffects, tooltip::add, durationScale, context.tickRate());
            }
        } catch (UnsupportedOperationException ignored) {
            // Immutable lines, e.g. HIDE_TOOLTIP handled above, plus any other cause.
        }
    }

    public void onRenderTooltip(GuiGraphicsExtractor graphics, FoodOverlay overlay, int tooltipX, int tooltipY, Font font) {
        int x = tooltipX;
        int y = tooltipY;

        // Hunger row, right to left so the icons face the right way.
        x += (overlay.hungerBars - 1) * 9;
        for (int i = 0; i < overlay.hungerBars * 2; i += 2) {
            Identifier background = TextureHelper.getFoodTexture(overlay.rotten, TextureHelper.FoodType.EMPTY);
            graphics.blitSprite(GUI_TEXTURED, background, x, y, 9, 9, ARGB.white(0.25F));

            if (Math.abs(overlay.hunger) > i) {
                boolean half = Math.abs(overlay.hunger) - 1 == i;
                Identifier icon = TextureHelper.getFoodTexture(
                        overlay.rotten, half ? TextureHelper.FoodType.HALF : TextureHelper.FoodType.FULL);
                graphics.blitSprite(GUI_TEXTURED, icon, x, y, 9, 9);
            }
            x -= 9;
        }
        if (overlay.hungerBarsText != null) {
            x += 18;
            Matrix3x2fStack pose = graphics.pose();
            pose.pushMatrix();
            pose.translate(x, y);
            pose.scale(0.75F, 0.75F);
            graphics.text(font, overlay.hungerBarsText, 2, 2, 0xFFAAAAAA);
            pose.popMatrix();
        }

        // Saturation row, same fill thresholds as the HUD overlay.
        x = tooltipX;
        y += 10;
        float absSaturation = Math.abs(overlay.saturation);
        x += (overlay.saturationBars - 1) * 7;
        for (int i = 0; i < overlay.saturationBars * 2; i += 2) {
            float fill = (absSaturation - i) / 2f;
            int u = fill >= 1 ? 21 : fill > 0.5 ? 14 : fill > 0.25 ? 7 : fill > 0 ? 0 : 28;
            int v = overlay.saturation >= 0 ? 27 : 34;
            int color = absSaturation <= i ? ARGB.white(0.5F) : ARGB.white(1.0F);
            graphics.blit(GUI_TEXTURED, TextureHelper.MOD_ICONS, x, y, u, v, 7, 7, 256, 256, color);
            x -= 7;
        }
        if (overlay.saturationBarsText != null) {
            x += 14;
            Matrix3x2fStack pose = graphics.pose();
            pose.pushMatrix();
            pose.translate(x, y);
            pose.scale(0.75F, 0.75F);
            graphics.text(font, overlay.saturationBarsText, 2, 1, 0xFFAAAAAA);
            pose.popMatrix();
        }
    }
}
