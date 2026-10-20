package com.aura.lamplight.ambrosia.food;

import com.mojang.datafixers.util.Function5;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

// this is genuinely just broken
public record CookingPotRecipe(
        List<Ingredient> ingredients,
        java.util.Optional<Ingredient> container,
        Holder<Item> result,
        int count,
        float experience,
        int cookingTime
) implements Recipe<CookingPotRecipeInput> {
    public static final int MAX_INGREDIENTS = 6;
    public static final int DEFAULT_COOKING_TIME = 200;

    private record Result(Holder<Item> item, int count) {
        static final MapCodec<Result> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("id").forGetter(Result::item),
                Codec.INT.optionalFieldOf("count", 1).forGetter(Result::count)
        ).apply(instance, Result::new));
    }

    public static final MapCodec<CookingPotRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.listOf(1, MAX_INGREDIENTS).fieldOf("ingredients").forGetter(CookingPotRecipe::ingredients),
            Ingredient.CODEC.optionalFieldOf("container").forGetter(CookingPotRecipe::container),
            Result.MAP_CODEC.fieldOf("result").forGetter(recipe -> new Result(recipe.result(), recipe.count())),
            Codec.FLOAT.optionalFieldOf("experience", 0.0F).forGetter(CookingPotRecipe::experience),
            Codec.INT.optionalFieldOf("cookingtime", DEFAULT_COOKING_TIME).forGetter(CookingPotRecipe::cookingTime)
    ).apply(instance, (ings, cont, res, xp, time) -> new CookingPotRecipe(ings, cont, res.item(), res.count(), xp, time)));

    @SuppressWarnings("all")
    public static final StreamCodec<RegistryFriendlyByteBuf, CookingPotRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), CookingPotRecipe::ingredients,
                    Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC, CookingPotRecipe::container,
                    ItemStack.STREAM_CODEC, CookingPotRecipe::resultStack,
                    ByteBufCodecs.FLOAT, CookingPotRecipe::experience,
                    ByteBufCodecs.VAR_INT, CookingPotRecipe::cookingTime,
                    (Function5<List<Ingredient>, java.util.Optional<Ingredient>, ItemStack, Float, Integer, CookingPotRecipe>)
                            (ings, cont, stack, xp, time) -> new CookingPotRecipe(
                                    ings, cont, stack.getItem().builtInRegistryHolder(), stack.getCount(), xp, time));

    private ItemStack resultStack() {
        return new ItemStack(this.result, this.count);
    }

    @Override
    public boolean matches(CookingPotRecipeInput input, net.minecraft.world.level.Level level) {
        List<ItemStack> available = new ArrayList<>();
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                available.add(stack);
            }
        }
        if (available.size() < this.ingredients.size()) {
            return false;
        }
        List<Ingredient> remaining = new ArrayList<>(this.ingredients);
        for (ItemStack stack : available) {
            boolean matched = false;
            for (int i = 0; i < remaining.size(); i++) {
                if (remaining.get(i).test(stack)) {
                    remaining.remove(i);
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                return false;
            }
        }
        return remaining.isEmpty();
    }

    @Override
    public ItemStack assemble(CookingPotRecipeInput input) {
        return new ItemStack(this.result, this.count);
    }

    @Override
    public boolean showNotification() {
        return true;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public RecipeSerializer<CookingPotRecipe> getSerializer() {
        return ModRecipes.COOKING_POT_SERIALIZER;
    }

    @Override
    public RecipeType<CookingPotRecipe> getType() {
        return ModRecipes.COOKING_POT_TYPE;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }
}
