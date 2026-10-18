package com.aura.lamplight.panacea.registry;

import net.fabricmc.fabric.api.registry.FabricPotionBrewingBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;

public class ModPotionRecipes {

    public static void register() {
        FabricPotionBrewingBuilder.BUILD.register(builder -> {
            builder.addMix(Potions.TURTLE_MASTER, Items.FERMENTED_SPIDER_EYE, ModPotions.VULNERABILITY);
            builder.addMix(Potions.LONG_TURTLE_MASTER, Items.FERMENTED_SPIDER_EYE, ModPotions.LONG_VULNERABILITY);
            builder.addMix(Potions.STRONG_TURTLE_MASTER, Items.FERMENTED_SPIDER_EYE, ModPotions.STRONG_VULNERABILITY);

            builder.addMix(Potions.FIRE_RESISTANCE, Items.FERMENTED_SPIDER_EYE, ModPotions.FROST_RESISTANCE);
            builder.addMix(Potions.LONG_FIRE_RESISTANCE, Items.FERMENTED_SPIDER_EYE, ModPotions.LONG_FROST_RESISTANCE);

            builder.addStartMix(Items.AZURE_BLUET, ModPotions.BLINDNESS);
            builder.addMix(ModPotions.BLINDNESS, Items.REDSTONE, ModPotions.LONG_BLINDNESS);

            builder.addStartMix(Items.SCULK, ModPotions.DARKNESS);
            builder.addMix(ModPotions.DARKNESS, Items.REDSTONE, ModPotions.LONG_DARKNESS);

            builder.addStartMix(Items.GLOW_BERRIES, ModPotions.GLOWING);
            builder.addMix(ModPotions.GLOWING, Items.REDSTONE, ModPotions.LONG_GLOWING);

            builder.addStartMix(Items.HONEY_BOTTLE, ModPotions.HASTE);
            builder.addMix(ModPotions.HASTE, Items.REDSTONE, ModPotions.LONG_HASTE);
            builder.addMix(ModPotions.HASTE, Items.FERMENTED_SPIDER_EYE, ModPotions.MINING_FATIGUE);
            builder.addMix(ModPotions.LONG_HASTE, Items.FERMENTED_SPIDER_EYE, ModPotions.LONG_MINING_FATIGUE);

            builder.addStartMix(Items.GOLDEN_APPLE, ModPotions.HEALTH_BOOST);
            builder.addMix(ModPotions.HEALTH_BOOST, Items.REDSTONE, ModPotions.LONG_HEALTH_BOOST);
            builder.addMix(ModPotions.HEALTH_BOOST, Items.GLOWSTONE_DUST, ModPotions.STRONG_HEALTH_BOOST);

            builder.addStartMix(Items.SHULKER_SHELL, ModPotions.LEVITATION);

            builder.addStartMix(Items.DIAMOND, Potions.LUCK);
            builder.addMix(Potions.LUCK, Items.FERMENTED_SPIDER_EYE, ModPotions.UNLUCK);

            builder.addMix(Potions.REGENERATION, Items.FERMENTED_SPIDER_EYE, ModPotions.WITHER);
            builder.addMix(Potions.LONG_REGENERATION, Items.FERMENTED_SPIDER_EYE, ModPotions.LONG_WITHER);
            builder.addMix(Potions.STRONG_REGENERATION, Items.FERMENTED_SPIDER_EYE, ModPotions.LONG_WITHER);

            builder.addMix(Potions.INVISIBILITY, Items.GLOWSTONE_DUST, ModPotions.SHROUDED_INVISIBILITY);
            builder.addMix(Potions.LONG_INVISIBILITY, Items.GLOWSTONE_DUST, ModPotions.SHROUDED_INVISIBILITY);

            builder.addMix(Potions.NIGHT_VISION, Items.GLOWSTONE_DUST, ModPotions.TRUE_SIGHT);
            builder.addMix(Potions.LONG_NIGHT_VISION, Items.GLOWSTONE_DUST, ModPotions.TRUE_SIGHT);
        });
    }
}
