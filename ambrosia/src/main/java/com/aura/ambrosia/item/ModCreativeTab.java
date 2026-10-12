package com.aura.ambrosia.item;

import com.aura.ambrosia.Ambrosia;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class ModCreativeTab {
    private ModCreativeTab() {}

    private static final net.minecraft.world.item.Item[] STATIONS = {
            ModItems.COOKING_POT, ModItems.WINE_RACK, ModItems.CUTTING_BOARD,
            ModItems.MIXING_BOWL, ModItems.MIXING_BOWL_SPOON, ModItems.PLATE,
    };

    private static final net.minecraft.world.item.Item[] KNIVES = {
            ModItems.WOODEN_KNIFE, ModItems.STONE_KNIFE, ModItems.COPPER_KNIFE, ModItems.IRON_KNIFE,
            ModItems.GOLDEN_KNIFE, ModItems.DIAMOND_KNIFE, ModItems.NETHERITE_KNIFE, ModItems.RITUAL_DAGGER,
    };

    public static final CreativeModeTab AMBROSIA_TAB = FabricCreativeModeTab.builder()
            .icon(() -> new ItemStack(ModItems.ROAST_DINNER))
            .title(Component.translatable("itemGroup.ambrosia"))
            .displayItems((params, output) -> {
                output.accept(ModItems.FRYING_PAN);
                output.accept(ModItems.SOUL_PEPPER);
                output.accept(ModItems.SEEDS_SOUL_PEPPER);
                for (var food : ModItems.FOODS) {
                    output.accept(food);
                }
                for (var ingredient : ModItems.INGREDIENTS) {
                    output.accept(ingredient);
                }
                for (var station : STATIONS) {
                    output.accept(station);
                }
                for (var knife : KNIVES) {
                    output.accept(knife);
                }
            })
            .build();

    public static void register() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
                Identifier.fromNamespaceAndPath(Ambrosia.MOD_ID, "ambrosia"), AMBROSIA_TAB);
        Ambrosia.LOGGER.info("Registering creative tab for {}", Ambrosia.MOD_ID);
    }
}
