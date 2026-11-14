package com.aura.lamplight.ambrosia.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record CookingPotRecipe(PotFluid base, List<Item> ingredients, PotFluid result, int cookTime) {
    public static final int DEFAULT_COOK_TIME = 100;

    public static final Codec<CookingPotRecipe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PotFluid.CODEC.fieldOf("base").forGetter(CookingPotRecipe::base),
            BuiltInRegistries.ITEM.byNameCodec().listOf().fieldOf("ingredients").forGetter(CookingPotRecipe::ingredients),
            PotFluid.CODEC.fieldOf("result").forGetter(CookingPotRecipe::result),
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("cook_time", DEFAULT_COOK_TIME).forGetter(CookingPotRecipe::cookTime)
    ).apply(instance, CookingPotRecipe::new));
    
    public boolean matches(PotFluid fluid, List<ItemStack> potIngredients) {
        if (fluid != this.base || potIngredients.size() != this.ingredients.size()) {
            return false;
        }
        Set<Item> potItems = new HashSet<>();
        for (ItemStack stack : potIngredients) {
            potItems.add(stack.getItem());
        }
        return potItems.equals(new HashSet<>(this.ingredients));
    }
}
