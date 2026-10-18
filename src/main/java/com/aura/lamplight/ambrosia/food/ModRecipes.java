package com.aura.lamplight.ambrosia.food;

import com.aura.lamplight.ambrosia.Ambrosia;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public final class ModRecipes {
    private ModRecipes() {}

    public static final RecipeType<CookingPotRecipe> COOKING_POT_TYPE = Registry.register(
            BuiltInRegistries.RECIPE_TYPE, Ambrosia.id("cooking_pot"), new RecipeType<>() {
                @Override
                public String toString() {
                    return Ambrosia.MOD_ID + ":cooking_pot";
                }
            });

    public static final RecipeSerializer<CookingPotRecipe> COOKING_POT_SERIALIZER = Registry.register(
            BuiltInRegistries.RECIPE_SERIALIZER, Ambrosia.id("cooking_pot"),
            new RecipeSerializer<>(CookingPotRecipe.MAP_CODEC, CookingPotRecipe.STREAM_CODEC));

    public static void register() {
        Ambrosia.LOGGER.info("Registering recipe types for {}", Ambrosia.MOD_ID);
    }
}
