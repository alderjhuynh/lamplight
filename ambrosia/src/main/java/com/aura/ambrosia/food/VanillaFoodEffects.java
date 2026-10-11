package com.aura.ambrosia.food;

import net.minecraft.world.item.Items;

public final class VanillaFoodEffects {
    private VanillaFoodEffects() {
    }

    public static void register() {
        // stews
        VanillaFoodTweaks.register(Items.RABBIT_STEW,    10, 20, VanillaFoodTweaks.ONE_MINUTE, VanillaFoodTweaks.FIVE_MINUTES);
        VanillaFoodTweaks.register(Items.MUSHROOM_STEW,   8, 12, VanillaFoodTweaks.ONE_MINUTE, VanillaFoodTweaks.ONE_MINUTE * 3);
        VanillaFoodTweaks.register(Items.BEETROOT_SOUP,   7, 10, VanillaFoodTweaks.ONE_MINUTE, VanillaFoodTweaks.ONE_MINUTE * 3);
        VanillaFoodTweaks.register(Items.SUSPICIOUS_STEW, 6,  7, VanillaFoodTweaks.NO_EFFECT,  VanillaFoodTweaks.ONE_MINUTE * 2);

        // sweets
        VanillaFoodTweaks.register(Items.PUMPKIN_PIE,  8, 10, VanillaFoodTweaks.ONE_MINUTE * 2, VanillaFoodTweaks.NO_EFFECT);
        VanillaFoodTweaks.register(Items.COOKIE,       2,  1, VanillaFoodTweaks.ONE_MINUTE,     VanillaFoodTweaks.NO_EFFECT);
        VanillaFoodTweaks.register(Items.HONEY_BOTTLE, 6,  4, VanillaFoodTweaks.ONE_MINUTE,     VanillaFoodTweaks.NO_EFFECT);

        // snacks
        VanillaFoodTweaks.register(Items.APPLE,        4, 4, VanillaFoodTweaks.ONE_MINUTE / 2, VanillaFoodTweaks.NO_EFFECT);
        VanillaFoodTweaks.register(Items.MELON_SLICE,  2, 2, VanillaFoodTweaks.ONE_MINUTE / 2, VanillaFoodTweaks.NO_EFFECT);
        VanillaFoodTweaks.register(Items.SWEET_BERRIES, 2, 1, VanillaFoodTweaks.ONE_MINUTE / 2, VanillaFoodTweaks.NO_EFFECT);
        VanillaFoodTweaks.register(Items.GLOW_BERRIES,  2, 1, VanillaFoodTweaks.ONE_MINUTE / 2, VanillaFoodTweaks.NO_EFFECT);

        // meat
        VanillaFoodTweaks.register(Items.COOKED_PORKCHOP, 8, 13, VanillaFoodTweaks.NO_EFFECT, VanillaFoodTweaks.ONE_MINUTE);
        VanillaFoodTweaks.register(Items.COOKED_BEEF,     8, 13, VanillaFoodTweaks.NO_EFFECT, VanillaFoodTweaks.ONE_MINUTE);
        VanillaFoodTweaks.register(Items.COOKED_MUTTON,   6, 10, VanillaFoodTweaks.NO_EFFECT, VanillaFoodTweaks.ONE_MINUTE);
        VanillaFoodTweaks.register(Items.COOKED_CHICKEN,  6,  7, VanillaFoodTweaks.NO_EFFECT, VanillaFoodTweaks.ONE_MINUTE);
        VanillaFoodTweaks.register(Items.COOKED_RABBIT,   6,  7, VanillaFoodTweaks.NO_EFFECT, VanillaFoodTweaks.NO_EFFECT);

        // light staples
        VanillaFoodTweaks.register(Items.COOKED_SALMON, 6, 10, VanillaFoodTweaks.NO_EFFECT, VanillaFoodTweaks.NO_EFFECT);
        VanillaFoodTweaks.register(Items.COOKED_COD,    5,  6, VanillaFoodTweaks.NO_EFFECT, VanillaFoodTweaks.NO_EFFECT);
        VanillaFoodTweaks.register(Items.BAKED_POTATO,  5,  7, VanillaFoodTweaks.NO_EFFECT, VanillaFoodTweaks.NO_EFFECT); 
        VanillaFoodTweaks.register(Items.BREAD,         5,  6, VanillaFoodTweaks.NO_EFFECT, VanillaFoodTweaks.NO_EFFECT);
        VanillaFoodTweaks.register(Items.CARROT,        3,  5, VanillaFoodTweaks.NO_EFFECT, VanillaFoodTweaks.NO_EFFECT); 
        VanillaFoodTweaks.register(Items.BEETROOT,      2,  2, VanillaFoodTweaks.NO_EFFECT, VanillaFoodTweaks.NO_EFFECT); 
        VanillaFoodTweaks.register(Items.DRIED_KELP,    1,  1, VanillaFoodTweaks.NO_EFFECT, VanillaFoodTweaks.NO_EFFECT);

        // golden foods
        VanillaFoodTweaks.register(Items.GOLDEN_CARROT,         6, 14, VanillaFoodTweaks.NO_EFFECT, VanillaFoodTweaks.ONE_MINUTE * 3);
        VanillaFoodTweaks.register(Items.GOLDEN_APPLE,          4, 10, VanillaFoodTweaks.NO_EFFECT, VanillaFoodTweaks.ONE_MINUTE * 2);
        VanillaFoodTweaks.register(Items.ENCHANTED_GOLDEN_APPLE, 4, 10, VanillaFoodTweaks.NO_EFFECT, VanillaFoodTweaks.FIVE_MINUTES);
    }
}
