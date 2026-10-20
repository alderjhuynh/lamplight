package com.aura.lamplight.ambrosia.client.appleskin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;

@SuppressWarnings("all")
public final class AppleSkin {
    public static final AppleSkin INSTANCE = new AppleSkin();

    // Baked-in settings (were Zephyr module settings; edit + rebuild to change).
    public static final boolean SHOW_SATURATION_OVERLAY = true;
    public static final boolean SHOW_EXHAUSTION_UNDERLAY = true;
    public static final boolean SHOW_FOOD_VALUES_OVERLAY = true;
    public static final boolean SHOW_HEALTH_OVERLAY = false;
    public static final boolean CHECK_OFFHAND = true;
    public static final boolean SHOW_VANILLA_ANIMATIONS = true;
    public static final double MAX_FLASH_ALPHA = 1.0;

    private final HudOverlayHandler overlay = new HudOverlayHandler();

    private float unclampedFlashAlpha = 0f;
    private float flashAlpha = 0f;
    private byte alphaDir = 1;

    private AppleSkin() {
    }

    /** Advances the HUD overlay flash animation towards its next alpha peak each tick. */
    public void tick(Minecraft client) {
        unclampedFlashAlpha += alphaDir * 0.125F;
        if (unclampedFlashAlpha >= 1.5F) {
            alphaDir = -1;
        } else if (unclampedFlashAlpha <= -0.5F) {
            alphaDir = 1;
        }
        flashAlpha = Math.max(0F, Math.min(1F, unclampedFlashAlpha)) * (float) MAX_FLASH_ALPHA;
    }

    /** Called before the vanilla food bar is drawn; used for the exhaustion underlay. */
    public void onExtractFoodPre(GuiGraphicsExtractor graphics, Player player, int top, int right, int guiTicks) {
        if (player == null) return;
        if (!SHOW_EXHAUSTION_UNDERLAY) return;

        overlay.drawExhaustionOverlay(graphics, FoodHelper.exhaustionLevel(player.getFoodData()), right, top, 0.75F);
    }

    /** Called after the vanilla food bar is drawn; used for saturation + food-value overlays. */
    public void onExtractFoodPost(GuiGraphicsExtractor graphics, Player player, int top, int right, int guiTicks) {
        if (player == null) return;
        if (!shouldRenderAnyOverlays()) return;

        FoodData stats = player.getFoodData();

        if (SHOW_SATURATION_OVERLAY) {
            overlay.drawSaturationOverlay(graphics, 0, stats.getSaturationLevel(), player, right, top, 1.0F, guiTicks);
        }

        FoodHelper.QueriedFoodResult result = overlay.heldFood.result(guiTicks, player);
        if (result == null) {
            resetFlash();
            return;
        }

        if (SHOW_FOOD_VALUES_OVERLAY) {
            // calculate the final hunger and saturation
            int foodHunger = result.modifiedFoodComponent.nutrition();
            float foodSaturationIncrement = result.modifiedFoodComponent.saturation();

            // draw hunger overlay
            overlay.drawHungerOverlay(graphics, foodHunger, stats.getFoodLevel(), player, right, top,
                    flashAlpha, FoodHelper.isRotten(result.itemStack), guiTicks);

            int newFoodValue = stats.getFoodLevel() + foodHunger;
            float newSaturationValue = stats.getSaturationLevel() + foodSaturationIncrement;

            // draw saturation overlay of gained saturation
            if (SHOW_SATURATION_OVERLAY) {
                float saturationGained = newSaturationValue > newFoodValue
                        ? newFoodValue - stats.getSaturationLevel()
                        : foodSaturationIncrement;
                overlay.drawSaturationOverlay(graphics, saturationGained, stats.getSaturationLevel(),
                        player, right, top, flashAlpha, guiTicks);
            }
        }
    }

    /** Called after the vanilla health bar is drawn; used for the estimated-health overlay. */
    public void onExtractHealth(GuiGraphicsExtractor graphics, Player player, int left, int top, int guiTicks) {
        if (player == null) return;
        if (!shouldRenderAnyOverlays()) return;

        FoodHelper.QueriedFoodResult result = overlay.heldFood.result(guiTicks, player);
        if (result == null) {
            resetFlash();
            return;
        }

        if (shouldShowEstimatedHealth(player, guiTicks)) {
            float foodHealthIncrement = FoodHelper.getEstimatedHealthIncrement(player, result.itemStack, result.modifiedFoodComponent);
            float currentHealth = player.getHealth();
            float modifiedHealth = Math.min(currentHealth + foodHealthIncrement, player.getMaxHealth());

            if (currentHealth < modifiedHealth) {
                overlay.drawHealthOverlay(graphics, currentHealth, modifiedHealth, player, left, top, flashAlpha, guiTicks);
            }
        }
    }

    private void resetFlash() {
        unclampedFlashAlpha = flashAlpha = 0;
        alphaDir = 1;
    }

    private static boolean shouldRenderAnyOverlays() {
        return SHOW_FOOD_VALUES_OVERLAY || SHOW_SATURATION_OVERLAY || SHOW_HEALTH_OVERLAY;
    }

    private boolean shouldShowEstimatedHealth(Player player, int guiTicks) {
        if (!SHOW_HEALTH_OVERLAY) {
            return false;
        }

        // Offsets size is set to zero intentionally to disable rendering when health is infinite.
        if (overlay.barOffsets.healthBarOffsets(guiTicks, player).isEmpty()) {
            return false;
        }

        FoodData stats = player.getFoodData();

        // in the PEACEFUL mode, health will restore faster
        if (player.level().getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }

        // when the player has any changed health amount by any case, can't show estimated health
        // because the player will be confused about how much health would be restored/damaged
        if (stats.getFoodLevel() >= 18) {
            return false;
        }

        if (player.hasEffect(MobEffects.POISON)) {
            return false;
        }

        if (player.hasEffect(MobEffects.WITHER)) {
            return false;
        }

        if (player.hasEffect(MobEffects.REGENERATION)) {
            return false;
        }

        return true;
    }
}
