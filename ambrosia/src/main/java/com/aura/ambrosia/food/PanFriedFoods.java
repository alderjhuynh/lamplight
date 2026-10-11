package com.aura.ambrosia.food;

import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

public final class PanFriedFoods {
    private PanFriedFoods() {
    }

    public static final int BONUS_NUTRITION = 1;
    public static final float BONUS_SATURATION = 1.0F;

    public static final String MARKER_KEY = "ambrosia:pan_fried";

    private static Optional<net.minecraft.world.item.crafting.RecipeManager> recipes(Level level) {
        if (level == null) {
            return Optional.empty();
        }
        var server = level.getServer();
        if (server == null) {
            return Optional.empty();
        }
        return Optional.of(server.getRecipeManager());
    }

    public static Optional<ItemStack> findCookResult(Level level, ItemStack raw) {
        if (level == null || raw.isEmpty()) {
            return Optional.empty();
        }
        Optional<net.minecraft.world.item.crafting.RecipeManager> manager = recipes(level);
        if (manager.isEmpty()) {
            return Optional.empty();
        }
        SingleRecipeInput input = new SingleRecipeInput(raw);
        Optional<ItemStack> campfire = manager.get()
                .getRecipeFor(RecipeType.CAMPFIRE_COOKING, input, level)
                .map(holder -> holder.value().assemble(input));
        if (campfire.isPresent() && !campfire.get().isEmpty()) {
            return campfire;
        }
        return manager.get()
                .getRecipeFor(RecipeType.SMELTING, input, level)
                .map(holder -> holder.value().assemble(input))
                .filter(result -> !result.isEmpty());
    }

    public static boolean isPanFried(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.copyTag().getBooleanOr(MARKER_KEY, false);
    }

    public static boolean isUncookedFood(Level level, ItemStack stack) {
        if (stack.isEmpty() || isPanFried(stack)) {
            return false;
        }
        if (VanillaFoodTweaks.tweakedFood(stack) == null) {
            return false;
        }
        if (level != null) {
            Optional<net.minecraft.world.item.crafting.RecipeManager> manager = recipes(level);
            if (manager.isEmpty()) {
                return true;
            }
            if (findCookResult(level, stack).isPresent()) {
                return true;
            }
            if (isKnownCookedOutput(level, stack.getItem())) {
                return false;
            }
        }
        return true;
    }

    public static boolean isUncookedFoodClient(ItemStack stack) {
        if (stack.isEmpty() || isPanFried(stack)) {
            return false;
        }
        return stack.get(DataComponents.FOOD) != null;
    }

    private static boolean isKnownCookedOutput(Level level, Item item) {
        Optional<net.minecraft.world.item.crafting.RecipeManager> manager = recipes(level);
        if (manager.isEmpty()) {
            return false;
        }
        for (RecipeHolder<?> holder : manager.get().getRecipes()) {
            if (holder.value() instanceof net.minecraft.world.item.crafting.AbstractCookingRecipe cooking) {
                ItemStack result;
                try {
                    result = cooking.assemble(new SingleRecipeInput(new ItemStack(item)));
                } catch (Exception ignored) {
                    continue;
                }
                if (!result.isEmpty() && result.is(item)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Optional<ItemStack> makePanFried(ItemStack base) {
        if (base.isEmpty()) {
            return Optional.empty();
        }
        ItemStack result = base.copyWithCount(1);

        FoodProperties tweakedFood = VanillaFoodTweaks.tweakedFood(result);
        if (tweakedFood == null) {
            return Optional.empty();
        }
        Consumable tweakedConsumable = VanillaFoodTweaks.tweakedConsumable(result);

        result.set(DataComponents.FOOD, new FoodProperties(
                tweakedFood.nutrition() + BONUS_NUTRITION,
                tweakedFood.saturation() + BONUS_SATURATION,
                tweakedFood.canAlwaysEat()));
        if (tweakedConsumable != null) {
            result.set(DataComponents.CONSUMABLE, tweakedConsumable);
        }

        Component cookedName = result.getItem().getName(result);
        result.set(DataComponents.CUSTOM_NAME,
                Component.translatable("item.ambrosia.pan_fried", cookedName)
                        .withStyle(style -> style.withItalic(false)));


        CompoundTag tag = new CompoundTag();
        tag.putBoolean(MARKER_KEY, true);
        result.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        return Optional.of(result);
    }

    public static Optional<ItemStack> panFry(Level level, ItemStack raw) {
        Optional<ItemStack> cooked = level == null ? Optional.empty() : findCookResult(level, raw);
        ItemStack base = cooked.orElseGet(() -> raw.copyWithCount(1));
        if (base.isEmpty()) {
            return Optional.empty();
        }
        return makePanFried(base);
    }
}
