package com.aura.ambrosia.food;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public class CookingPotRecipeInput implements RecipeInput {
    private final List<ItemStack> items;

    public CookingPotRecipeInput(List<ItemStack> items) {
        if (items.size() != CookingPotRecipe.MAX_INGREDIENTS) {
            throw new IllegalArgumentException("expected " + CookingPotRecipe.MAX_INGREDIENTS + " stacks");
        }
        this.items = List.copyOf(items);
    }

    @Override
    public ItemStack getItem(int index) {
        return this.items.get(index);
    }

    @Override
    public int size() {
        return this.items.size();
    }
}
