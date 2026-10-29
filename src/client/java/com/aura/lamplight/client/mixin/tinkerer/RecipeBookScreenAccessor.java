package com.aura.lamplight.client.mixin.tinkerer;

import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractRecipeBookScreen.class)
public interface RecipeBookScreenAccessor {

	@Accessor("recipeBookComponent")
	RecipeBookComponent<?> lamplight$recipeBookComponent();
}
