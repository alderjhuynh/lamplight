package com.aura.lamplight.ambrosia.food;

import com.aura.lamplight.ambrosia.item.ModItems;
import net.minecraft.world.item.Item;

import static com.aura.lamplight.ambrosia.food.VanillaFoodTweaks.FIVE_MINUTES;
import static com.aura.lamplight.ambrosia.food.VanillaFoodTweaks.NO_EFFECT;
import static com.aura.lamplight.ambrosia.food.VanillaFoodTweaks.ONE_MINUTE;
import static com.aura.lamplight.ambrosia.food.VanillaFoodTweaks.THIRTY_SECONDS;

public final class ModFoods {
    private ModFoods() {
    }

    private static void food(Item item, int hunger, float saturation, int famished, int nourishment) {
        VanillaFoodTweaks.register(item, hunger, saturation, famished, nourishment, NO_EFFECT);
    }

    private static void comfort(Item item, int hunger, float saturation, int famished, int nourishment, int comfort) {
        VanillaFoodTweaks.register(item, hunger, saturation, famished, nourishment, comfort);
    }

    public static void register() {
        // produce
        food(ModItems.CORN, 3, 3.0F, NO_EFFECT, NO_EFFECT);
        food(ModItems.RICE, 2, 2.0F, NO_EFFECT, NO_EFFECT);
        food(ModItems.TOMATO, 3, 3.0F, THIRTY_SECONDS, NO_EFFECT);
        food(ModItems.LETTUCE, 2, 2.0F, THIRTY_SECONDS, NO_EFFECT);
        food(ModItems.GRAPES, 2, 2.0F, THIRTY_SECONDS, NO_EFFECT);
        food(ModItems.GINGER, 2, 2.0F, NO_EFFECT, NO_EFFECT);

        // ingredients
        food(ModItems.BUTTER, 1, 1.0F, NO_EFFECT, NO_EFFECT);
        food(ModItems.CHEESE, 4, 5.0F, NO_EFFECT, NO_EFFECT);
        food(ModItems.NOODLES, 3, 3.0F, NO_EFFECT, NO_EFFECT);
        food(ModItems.SALSA, 3, 3.0F, ONE_MINUTE, NO_EFFECT);
        food(ModItems.TORTILLA, 4, 4.0F, NO_EFFECT, NO_EFFECT);
        food(ModItems.CHOCOLATE_BAR, 3, 3.0F, ONE_MINUTE, NO_EFFECT);

        // raw cuts
        food(ModItems.BEEF_CUTLETS, 2, 1.0F, NO_EFFECT, NO_EFFECT);
        food(ModItems.CHICKEN_CUTLETS, 2, 1.0F, NO_EFFECT, NO_EFFECT);
        food(ModItems.MUTTON_CUTLETS, 2, 1.0F, NO_EFFECT, NO_EFFECT);
        food(ModItems.RABBIT_CUTLETS, 2, 1.0F, NO_EFFECT, NO_EFFECT);
        food(ModItems.COD_FILLETS, 1, 0.5F, NO_EFFECT, NO_EFFECT);
        food(ModItems.SALMON_FILLETS, 1, 0.5F, NO_EFFECT, NO_EFFECT);

        // snacks sweets & baked goods
        food(ModItems.BUTTERED_APPLE, 5, 5.0F, ONE_MINUTE, NO_EFFECT);
        food(ModItems.CARAMEL_APPLE, 5, 6.0F, ONE_MINUTE, NO_EFFECT);
        food(ModItems.CARAMEL_POPCORN, 3, 3.0F, ONE_MINUTE, NO_EFFECT);
        food(ModItems.CARROT_CAKE, 8, 10.0F, ONE_MINUTE * 2, NO_EFFECT);
        food(ModItems.CHEESECAKE, 8, 10.0F, ONE_MINUTE * 2, NO_EFFECT);
        food(ModItems.CHIPS_AND_DIP, 6, 7.0F, ONE_MINUTE, NO_EFFECT);
        food(ModItems.CHOCOLATE_CAKE, 8, 10.0F, ONE_MINUTE * 2, NO_EFFECT);
        food(ModItems.CHORUS_CUPCAKE, 4, 5.0F, ONE_MINUTE, NO_EFFECT);
        food(ModItems.CINNAMON_ROLL, 5, 6.0F, ONE_MINUTE, NO_EFFECT);
        food(ModItems.COFFEE_CAKE, 8, 10.0F, ONE_MINUTE * 2, NO_EFFECT);
        food(ModItems.DINO_NUGGET_STEGO, 3, 3.0F, THIRTY_SECONDS, NO_EFFECT);
        food(ModItems.DINO_NUGGET_TREX, 3, 3.0F, THIRTY_SECONDS, NO_EFFECT);
        food(ModItems.FORTUNE_COOKIE, 2, 2.0F, ONE_MINUTE, NO_EFFECT);
        food(ModItems.GLAZED_DONUT, 4, 5.0F, ONE_MINUTE, NO_EFFECT);
        food(ModItems.ICE_CREAM, 4, 5.0F, ONE_MINUTE, NO_EFFECT);
        food(ModItems.ICE_CREAM_SANDWICH, 6, 7.0F, ONE_MINUTE, NO_EFFECT);
        food(ModItems.POPCORN, 2, 2.0F, THIRTY_SECONDS, NO_EFFECT);
        food(ModItems.PROUD_COOKIE, 2, 2.0F, ONE_MINUTE, NO_EFFECT);
        food(ModItems.RAISIN_COOKIE, 2, 2.0F, ONE_MINUTE, NO_EFFECT);
        food(ModItems.RAISINS, 2, 2.0F, THIRTY_SECONDS, NO_EFFECT);
        food(ModItems.ROASTED_SEEDS, 2, 2.0F, THIRTY_SECONDS, NO_EFFECT);
        food(ModItems.TIRAMISU, 8, 10.0F, ONE_MINUTE * 2, NO_EFFECT);
        food(ModItems.TRAIL_MIX, 4, 5.0F, ONE_MINUTE, NO_EFFECT);
        food(ModItems.TORTILLA_CHIPS, 3, 3.0F, THIRTY_SECONDS, NO_EFFECT);
        food(ModItems.FRIES, 4, 5.0F, ONE_MINUTE, NO_EFFECT);
        food(ModItems.NACHOS, 8, 10.0F, ONE_MINUTE, ONE_MINUTE);

        // bowls
        food(ModItems.STEW, 9, 14.0F, ONE_MINUTE, ONE_MINUTE * 3);
        comfort(ModItems.TOMATO_SOUP, 7, 10.0F, ONE_MINUTE, ONE_MINUTE * 3, ONE_MINUTE);
        food(ModItems.VEGETABLE_SOUP, 8, 12.0F, ONE_MINUTE, ONE_MINUTE * 3);
        food(ModItems.MUSHROOM_RISOTTO, 8, 12.0F, ONE_MINUTE, ONE_MINUTE * 3);
        food(ModItems.RAMEN, 10, 14.0F, ONE_MINUTE, ONE_MINUTE * 3);
        food(ModItems.BEEF_STROGANOFF, 10, 14.0F, ONE_MINUTE, ONE_MINUTE * 3);
        food(ModItems.BUTTER_CHICKEN, 8, 12.0F, ONE_MINUTE, ONE_MINUTE);

        // feasts & baked dishes
        comfort(ModItems.ROAST_DINNER, 10, 16.0F, ONE_MINUTE, FIVE_MINUTES, ONE_MINUTE);
        food(ModItems.HONEY_GLAZED_HAM, 10, 14.0F, NO_EFFECT, ONE_MINUTE * 3);
        food(ModItems.SHEPHERDS_PIE, 9, 14.0F, ONE_MINUTE, ONE_MINUTE * 3);
        food(ModItems.STEAK_AND_ALE_PIE, 9, 14.0F, ONE_MINUTE, ONE_MINUTE * 3);
        food(ModItems.BEEF_WELLINGTON, 10, 16.0F, ONE_MINUTE, ONE_MINUTE * 3);
        food(ModItems.LASAGNA, 10, 14.0F, ONE_MINUTE, ONE_MINUTE);
        food(ModItems.SPAGHETTI, 8, 12.0F, ONE_MINUTE, ONE_MINUTE);
        comfort(ModItems.MAC_AND_CHEESE, 8, 12.0F, ONE_MINUTE, NO_EFFECT, ONE_MINUTE);
        food(ModItems.PIZZA, 10, 14.0F, ONE_MINUTE, ONE_MINUTE);

        // meat plates
        food(ModItems.GINGER_CHICKEN, 8, 10.0F, NO_EFFECT, ONE_MINUTE);
        food(ModItems.FRIED_CHICKEN, 8, 10.0F, NO_EFFECT, ONE_MINUTE);
        food(ModItems.BUFFALO_WINGS, 6, 8.0F, NO_EFFECT, ONE_MINUTE);
        food(ModItems.CHICKEN_SATAY, 6, 8.0F, NO_EFFECT, ONE_MINUTE);
        food(ModItems.KEBAB, 8, 10.0F, NO_EFFECT, ONE_MINUTE);
        food(ModItems.FRIED_FISH, 5, 7.0F, NO_EFFECT, NO_EFFECT);
        food(ModItems.FISH_AND_CHIPS, 8, 10.0F, ONE_MINUTE, NO_EFFECT);
        food(ModItems.SURF_N_TURF, 10, 16.0F, NO_EFFECT, ONE_MINUTE * 3);

        // rice boxes & noodle plates
        food(ModItems.BENTO_BOX, 10, 14.0F, ONE_MINUTE, ONE_MINUTE);
        food(ModItems.BIBIMBAP, 9, 12.0F, ONE_MINUTE, ONE_MINUTE);
        food(ModItems.EGG_FRIED_RICE, 8, 10.0F, ONE_MINUTE, NO_EFFECT);
        food(ModItems.PAD_THAI, 8, 12.0F, ONE_MINUTE, ONE_MINUTE);

        // handhelds & wraps
        food(ModItems.HAMBURGER, 8, 10.0F, NO_EFFECT, ONE_MINUTE);
        food(ModItems.CHEESEBURGER, 8, 11.0F, NO_EFFECT, ONE_MINUTE);
        food(ModItems.CHICKEN_SANDWICH, 8, 10.0F, NO_EFFECT, ONE_MINUTE);
        food(ModItems.GRILLED_CHEESE, 7, 9.0F, ONE_MINUTE, NO_EFFECT);
        food(ModItems.BLT, 8, 10.0F, ONE_MINUTE, NO_EFFECT);
        food(ModItems.PB_AND_J, 7, 9.0F, ONE_MINUTE * 2, NO_EFFECT);
        food(ModItems.BURRITO, 8, 12.0F, ONE_MINUTE, ONE_MINUTE);
        food(ModItems.BURRITO_BOX, 9, 13.0F, ONE_MINUTE, ONE_MINUTE);
        food(ModItems.TACO, 8, 10.0F, ONE_MINUTE, NO_EFFECT);
        food(ModItems.QUESADILLA, 7, 9.0F, ONE_MINUTE, NO_EFFECT);
        food(ModItems.GYRO, 8, 12.0F, ONE_MINUTE, ONE_MINUTE);
        food(ModItems.LETTUCE_WRAP, 5, 6.0F, ONE_MINUTE, NO_EFFECT);
        food(ModItems.SAUSAGE_ROLL, 8, 10.0F, NO_EFFECT, ONE_MINUTE);
        food(ModItems.POUTINE, 8, 12.0F, ONE_MINUTE, ONE_MINUTE);
        food(ModItems.CLASSIC_BREAKFAST, 10, 14.0F, ONE_MINUTE, ONE_MINUTE);
        food(ModItems.SPRING_SALAD, 5, 6.0F, ONE_MINUTE, NO_EFFECT);
        food(ModItems.SOURDOUGH_BREAD, 5, 7.0F, NO_EFFECT, NO_EFFECT);

        // drinks
        food(ModItems.COFFEE, 3, 3.0F, THIRTY_SECONDS, NO_EFFECT);
        comfort(ModItems.TEA, 3, 3.0F, THIRTY_SECONDS, NO_EFFECT, ONE_MINUTE);
        comfort(ModItems.HOT_CHOCOLATE, 4, 5.0F, ONE_MINUTE, NO_EFFECT, ONE_MINUTE);
        food(ModItems.APPLE_CIDER, 4, 5.0F, ONE_MINUTE, NO_EFFECT);
        food(ModItems.GINGER_BEER, 4, 5.0F, ONE_MINUTE, NO_EFFECT);

        // marshmallows
        food(ModItems.MARSHMALLOW, 2, 1.0F, NO_EFFECT, NO_EFFECT);
        comfort(ModItems.TOASTED_MARSHMALLOW, 4, 5.0F, ONE_MINUTE, NO_EFFECT, ONE_MINUTE);
        food(ModItems.BROWNED_MARSHMALLOW, 3, 4.0F, ONE_MINUTE, NO_EFFECT);
        food(ModItems.BURNT_MARSHMALLOW, 1, 1.0F, NO_EFFECT, NO_EFFECT);
        food(ModItems.VOLCANIC_MARSHMALLOW, 4, 5.0F, ONE_MINUTE, NO_EFFECT);

        // wines
        food(ModItems.WINE, 4, 4.0F, THIRTY_SECONDS, NO_EFFECT);
        food(ModItems.GRAPE_WINE, 4, 5.0F, THIRTY_SECONDS, NO_EFFECT);
        food(ModItems.FINE_WINE, 5, 6.0F, THIRTY_SECONDS, ONE_MINUTE);
        food(ModItems.SWEET_BERRY_WINE, 4, 4.0F, THIRTY_SECONDS, NO_EFFECT);
        comfort(ModItems.MULLED_WINE, 4, 5.0F, ONE_MINUTE, NO_EFFECT, ONE_MINUTE);
        food(ModItems.BLAZING_WINE, 4, 4.0F, ONE_MINUTE, NO_EFFECT);
        food(ModItems.BREEZING_WINE, 4, 4.0F, ONE_MINUTE, NO_EFFECT);
        food(ModItems.CREEPING_WINE, 4, 4.0F, ONE_MINUTE, NO_EFFECT);
        food(ModItems.DIVING_WINE, 4, 4.0F, ONE_MINUTE, NO_EFFECT);
        food(ModItems.GLEAMING_WINE, 4, 4.0F, ONE_MINUTE, NO_EFFECT);
        food(ModItems.BLESSING_WINE, 4, 4.0F, ONE_MINUTE, NO_EFFECT);
        food(ModItems.CHORUS_WINE, 4, 4.0F, ONE_MINUTE, NO_EFFECT);
        food(ModItems.HEROIC_WINE, 4, 5.0F, ONE_MINUTE, NO_EFFECT);
        food(ModItems.PROSPECTING_WINE, 4, 5.0F, ONE_MINUTE, NO_EFFECT);
    }
}
