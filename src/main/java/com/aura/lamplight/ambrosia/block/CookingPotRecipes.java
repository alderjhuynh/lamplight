package com.aura.lamplight.ambrosia.block;

import com.aura.lamplight.ambrosia.Ambrosia;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

@SuppressWarnings ("deprecation")
public class CookingPotRecipes extends SimpleJsonResourceReloadListener<CookingPotRecipe> implements IdentifiableResourceReloadListener {
    private static volatile List<CookingPotRecipe> LOADED = List.of();

    public CookingPotRecipes() {
        super(CookingPotRecipe.CODEC, FileToIdConverter.json("pot_recipes"));
    }

    @Override
    protected void apply(Map<Identifier, CookingPotRecipe> data, ResourceManager manager, ProfilerFiller profiler) {
        LOADED = List.copyOf(data.values());
        Ambrosia.LOGGER.info("Loaded {} cooking pot recipes", LOADED.size());
    }

    @Override
    public Identifier getFabricId() {
        return Ambrosia.id("pot_recipes");
    }

    public static @Nullable CookingPotRecipe find(PotFluid fluid, List<ItemStack> ingredients) {
        for (CookingPotRecipe recipe : LOADED) {
            if (recipe.matches(fluid, ingredients)) {
                return recipe;
            }
        }
        return null;
    }

    public static void initialize() {
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new CookingPotRecipes());
    }
}
