package com.aura.lamplight.ambrosia.item;

import com.aura.lamplight.ambrosia.Ambrosia;
import com.aura.lamplight.ambrosia.block.ModBlocks;
import java.util.function.Function;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.Foods;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.TeleportRandomlyConsumeEffect;
import net.minecraft.world.level.block.Block;

public final class ModItems {
    private ModItems() {}

    public static final Item FRYING_PAN = register("frying_pan", FryingPanItem::new, new Item.Properties().stacksTo(1));
    public static final Item SOUL_PEPPER =
            register("soul_pepper", SoulPepperItem::new, new Item.Properties().food(Foods.CARROT));
    public static final Item SEEDS_SOUL_PEPPER =
            register("seeds_soul_pepper", properties -> new BlockItem(ModBlocks.SOUL_PEPPER_CROP, properties), new Item.Properties());

    public static final Item WINE_RACK = blockItem("wine_rack", ModBlocks.WINE_RACK);

    public static final Item WOODEN_KNIFE = knife("wooden_knife", net.minecraft.world.item.ToolMaterial.WOOD);
    public static final Item STONE_KNIFE = knife("stone_knife", net.minecraft.world.item.ToolMaterial.STONE);
    public static final Item COPPER_KNIFE = knife("copper_knife", net.minecraft.world.item.ToolMaterial.COPPER);
    public static final Item IRON_KNIFE = knife("iron_knife", net.minecraft.world.item.ToolMaterial.IRON);
    public static final Item GOLDEN_KNIFE = knife("golden_knife", net.minecraft.world.item.ToolMaterial.GOLD);
    public static final Item DIAMOND_KNIFE = knife("diamond_knife", net.minecraft.world.item.ToolMaterial.DIAMOND);
    public static final Item NETHERITE_KNIFE = knife("netherite_knife", net.minecraft.world.item.ToolMaterial.NETHERITE);

    public static final Item CORN_SEEDS = seed("corn_seeds", ModBlocks.CORN_CROP);
    public static final Item RICE_SEEDS = seed("rice_seeds", ModBlocks.RICE_CROP);
    public static final Item TOMATO_SEEDS = seed("tomato_seeds", ModBlocks.TOMATO_CROP);
    public static final Item LETTUCE_SEEDS = seed("lettuce_seeds", ModBlocks.LETTUCE_CROP);
    public static final Item GRAPE_SEEDS = seed("grape_seeds", ModBlocks.GRAPE_CROP);
    public static final Item COFFEE_CHERRIES = seed("coffee_cherries", ModBlocks.COFFEE_CROP);

    public static final Item CORN = food("corn", 3, 3.0F);
    public static final Item RICE = food("rice", 2, 2.0F);
    public static final Item TOMATO = food("tomato", 3, 3.0F);
    public static final Item LETTUCE = food("lettuce", 2, 2.0F);
    public static final Item GRAPES = food("grapes", 2, 2.0F);
    public static final Item GINGER = food("ginger", 2, 2.0F);
    public static final Item COFFEE_BEANS = plain("coffee_beans");
    public static final Item CINNAMON = plain("cinnamon");

    public static final Item BUTTER = food("butter", 1, 1.0F);
    public static final Item CHEESE = food("cheese", 4, 5.0F);
    public static final Item DOUGH = plain("dough");
    public static final Item MILK_BOTTLE = register("milk_bottle", Item::new, new Item.Properties().craftRemainder(Items.GLASS_BOTTLE));
    public static final Item NOODLES = food("noodles", 3, 3.0F);
    public static final Item CHOCOLATE_BAR = food("chocolate_bar", 3, 3.0F);

    public static final Item BEEF_CUTLETS = food("beef_cutlets", 2, 1.0F);
    public static final Item CHICKEN_CUTLETS = food("chicken_cutlets", 2, 1.0F);
    public static final Item MUTTON_CUTLETS = food("mutton_cutlets", 2, 1.0F);
    public static final Item RABBIT_CUTLETS = food("rabbit_cutlets", 2, 1.0F);
    public static final Item COD_FILLETS = food("cod_fillets", 1, 0.5F);
    public static final Item SALMON_FILLETS = food("salmon_fillets", 1, 0.5F);

    public static final Item BUTTERED_APPLE = food("buttered_apple", 5, 5.0F);
    public static final Item CARAMEL_APPLE = food("caramel_apple", 5, 6.0F);
    public static final Item CARROT_CAKE = food("carrot_cake", 8, 10.0F);
    public static final Item CHEESECAKE = food("cheesecake", 8, 10.0F);
    public static final Item CHORUS_CUPCAKE = register("chorus_cupcake", Item::new, new Item.Properties().food(new FoodProperties(4, 5.0F, false), Consumables.CHORUS_FRUIT));
    public static final Item DINO_NUGGET_STEGO = food("dino_nugget_stego", 3, 3.0F);
    public static final Item DINO_NUGGET_TREX = food("dino_nugget_trex", 3, 3.0F);
    public static final Item FRIES = food("fries", 4, 5.0F);

    public static final Item STEW = bowlFood("stew", 9, 14.0F);
    public static final Item TOMATO_SOUP = bowlFood("tomato_soup", 7, 10.0F);
    public static final Item VEGETABLE_SOUP = bowlFood("vegetable_soup", 8, 12.0F);
    public static final Item MUSHROOM_RISOTTO = bowlFood("mushroom_risotto", 8, 12.0F);
    public static final Item BEEF_STROGANOFF = bowlFood("beef_stroganoff", 10, 14.0F);
    public static final Item ROAST_DINNER = food("roast_dinner", 10, 16.0F);
    public static final Item HONEY_GLAZED_HAM = food("honey_glazed_ham", 10, 14.0F);
    public static final Item SHEPHERDS_PIE = food("shepherds_pie", 9, 14.0F);
    public static final Item SPAGHETTI = food("spaghetti", 8, 12.0F);
    public static final Item FRIED_CHICKEN = food("fried_chicken", 8, 10.0F);
    public static final Item CHICKEN_SATAY = food("chicken_satay", 6, 8.0F);
    public static final Item KEBAB = food("kebab", 8, 10.0F);
    public static final Item FRIED_FISH = food("fried_fish", 5, 7.0F);
    public static final Item FISH_AND_CHIPS = food("fish_and_chips", 8, 10.0F);
    public static final Item SURF_N_TURF = food("surf_n_turf", 10, 16.0F);
    public static final Item EGG_FRIED_RICE = food("egg_fried_rice", 8, 10.0F);
    public static final Item HAMBURGER = food("hamburger", 8, 10.0F);
    public static final Item CHICKEN_SANDWICH = food("chicken_sandwich", 8, 10.0F);
    public static final Item GRILLED_CHEESE = food("grilled_cheese", 7, 9.0F);
    public static final Item BLT = food("blt", 8, 10.0F);
    public static final Item PB_AND_J = food("pb_and_j", 7, 9.0F);
    public static final Item BURRITO = food("burrito", 8, 12.0F);
    public static final Item GYRO = food("gyro", 8, 12.0F);
    public static final Item LETTUCE_WRAP = food("lettuce_wrap", 5, 6.0F);
    public static final Item POUTINE = food("poutine", 8, 12.0F);
    public static final Item SOURDOUGH_BREAD = food("sourdough_bread", 5, 7.0F);

    public static final Item COFFEE = drink("coffee", 3, 3.0F);
    public static final Item HOT_CHOCOLATE = drink("hot_chocolate", 4, 5.0F);
    public static final Item APPLE_CIDER = drink("apple_cider", 4, 5.0F);

    public static final Item WINE = drink("wine", 4, 4.0F);
    public static final Item GRAPE_WINE = drink("grape_wine", 4, 5.0F);
    public static final Item FINE_WINE = drink("fine_wine", 5, 6.0F);
    public static final Item SWEET_BERRY_WINE = drink("sweet_berry_wine", 4, 4.0F);
    public static final Item MULLED_WINE = drink("mulled_wine", 4, 5.0F);
    public static final Item BLAZING_WINE = drinkWithEffect("blazing_wine", 4, 4.0F,
            Consumables.defaultDrink().onConsume(new ApplyStatusEffectsConsumeEffect(
                    new MobEffectInstance(net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE, 3600), 1.0F)).build());
    public static final Item BREEZING_WINE = drinkWithEffect("breezing_wine", 4, 4.0F,
            Consumables.defaultDrink().onConsume(new ApplyStatusEffectsConsumeEffect(
                    new MobEffectInstance(net.minecraft.world.effect.MobEffects.WIND_CHARGED, 1200), 1.0F)).build());
    public static final Item CREEPING_WINE = drinkWithEffect("creeping_wine", 4, 4.0F,
            Consumables.defaultDrink().onConsume(new ApplyStatusEffectsConsumeEffect(
                    new MobEffectInstance(net.minecraft.world.effect.MobEffects.INFESTED, 3600), 1.0F)).build());
    public static final Item DIVING_WINE = drinkWithEffect("diving_wine", 4, 4.0F,
            Consumables.defaultDrink().onConsume(new ApplyStatusEffectsConsumeEffect(
                    new MobEffectInstance(net.minecraft.world.effect.MobEffects.WATER_BREATHING, 3600), 1.0F)).build());
    public static final Item GLEAMING_WINE = drinkWithEffect("gleaming_wine", 4, 4.0F,
            Consumables.defaultDrink().onConsume(new ApplyStatusEffectsConsumeEffect(
                    new MobEffectInstance(net.minecraft.world.effect.MobEffects.GLOWING, 3600), 1.0F)).build());
    public static final Item BLESSING_WINE = drinkWithEffect("blessing_wine", 4, 4.0F,
            Consumables.defaultDrink().onConsume(new ApplyStatusEffectsConsumeEffect(
                    new MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 400), 1.0F)).build());
    public static final Item CHORUS_WINE = drinkWithEffect("chorus_wine", 4, 4.0F,
            Consumables.defaultDrink().onConsume(new TeleportRandomlyConsumeEffect(16.0F)).build());
    public static final Item HEROIC_WINE = drinkWithEffect("heroic_wine", 4, 5.0F,
            Consumables.defaultDrink().onConsume(new ApplyStatusEffectsConsumeEffect(
                    new MobEffectInstance(net.minecraft.world.effect.MobEffects.HERO_OF_THE_VILLAGE, 3600), 1.0F)).build());
    public static final Item PROSPECTING_WINE = drinkWithEffect("prospecting_wine", 4, 5.0F,
            Consumables.defaultDrink().onConsume(new ApplyStatusEffectsConsumeEffect(
                    new MobEffectInstance(net.minecraft.world.effect.MobEffects.LUCK, 3600), 1.0F)).build());

    public static final Item MARSHMALLOW = food("marshmallow", 2, 1.0F);
    public static final Item TOASTED_MARSHMALLOW = food("toasted_marshmallow", 4, 5.0F);
    public static final Item BROWNED_MARSHMALLOW = food("browned_marshmallow", 3, 4.0F);
    public static final Item BURNT_MARSHMALLOW = food("burnt_marshmallow", 1, 1.0F);
    public static final Item VOLCANIC_MARSHMALLOW = food("volcanic_marshmallow", 4, 5.0F);

    private static Item plain(String name) {
        return register(name, Item::new, new Item.Properties());
    }

    private static Item food(String name, int hunger, float saturation) {
        return register(name, Item::new, new Item.Properties().food(new FoodProperties(hunger, saturation, false)));
    }

    private static Item bowlFood(String name, int hunger, float saturation) {
        return register(name, Item::new, new Item.Properties()
                .food(new FoodProperties(hunger, saturation, false))
                .usingConvertsTo(Items.BOWL));
    }

    private static Item drink(String name, int hunger, float saturation) {
        return register(name, Item::new, new Item.Properties()
                .food(new FoodProperties(hunger, saturation, true), Consumables.DEFAULT_DRINK));
    }

    private static Item drinkWithEffect(String name, int hunger, float saturation, net.minecraft.world.item.component.Consumable consumable) {
        return register(name, Item::new, new Item.Properties()
                .food(new FoodProperties(hunger, saturation, true), consumable));
    }

    private static Item seed(String name, Block crop) {
        return register(name, properties -> new BlockItem(crop, properties), new Item.Properties());
    }

    private static Item blockItem(String name, Block block) {
        return register(name, properties -> new BlockItem(block, properties), new Item.Properties());
    }

    private static Item knife(String name, net.minecraft.world.item.ToolMaterial material) {
        return register(name, Item::new, material.applySwordProperties(new Item.Properties(), 3.0F, -2.0F));
    }

    public static boolean isKnife(net.minecraft.world.item.ItemStack stack) {
        var item = stack.getItem();
        return item == WOODEN_KNIFE || item == STONE_KNIFE || item == COPPER_KNIFE || item == IRON_KNIFE
                || item == GOLDEN_KNIFE || item == DIAMOND_KNIFE || item == NETHERITE_KNIFE;
    }

    public static boolean isWine(net.minecraft.world.item.ItemStack stack) {
        var item = stack.getItem();
        return item == WINE || item == GRAPE_WINE || item == FINE_WINE || item == SWEET_BERRY_WINE
                || item == MULLED_WINE || item == BLAZING_WINE || item == BREEZING_WINE || item == CREEPING_WINE
                || item == DIVING_WINE || item == GLEAMING_WINE || item == BLESSING_WINE || item == CHORUS_WINE
                || item == HEROIC_WINE || item == PROSPECTING_WINE;
    }

    private static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties properties) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Ambrosia.MOD_ID, name));
        return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(properties.setId(key)));
    }

    private static final ResourceKey<CreativeModeTab> TOOLS_TAB =
            ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.withDefaultNamespace("tools"));
    private static final ResourceKey<CreativeModeTab> FOOD_TAB =
            ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.withDefaultNamespace("food"));
    private static final ResourceKey<CreativeModeTab> INGREDIENTS_TAB =
            ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.withDefaultNamespace("ingredients"));

    public static void initialize() {
        CreativeModeTabEvents.modifyOutputEvent(TOOLS_TAB).register((output) -> output.accept(FRYING_PAN));
        CreativeModeTabEvents.modifyOutputEvent(FOOD_TAB).register((output) -> {
            output.accept(SOUL_PEPPER);
            for (Item food : FOODS) {
                output.accept(food);
            }
        });
        CreativeModeTabEvents.modifyOutputEvent(INGREDIENTS_TAB).register((output) -> {
            output.accept(SEEDS_SOUL_PEPPER);
            for (Item ingredient : INGREDIENTS) {
                output.accept(ingredient);
            }
        });
        Ambrosia.LOGGER.info("Registering {} items for {}", 3 + FOODS.length + INGREDIENTS.length, Ambrosia.MOD_ID);
    }

    static final Item[] FOODS = {
            CORN, RICE, TOMATO, LETTUCE, GRAPES, GINGER, BUTTER, CHEESE, NOODLES,
            CHOCOLATE_BAR, BEEF_CUTLETS, CHICKEN_CUTLETS, MUTTON_CUTLETS, RABBIT_CUTLETS,
            COD_FILLETS, SALMON_FILLETS, BUTTERED_APPLE, CARAMEL_APPLE, CARROT_CAKE,
            CHEESECAKE, CHORUS_CUPCAKE, DINO_NUGGET_STEGO, DINO_NUGGET_TREX, FRIES,
            STEW, TOMATO_SOUP, VEGETABLE_SOUP, MUSHROOM_RISOTTO, BEEF_STROGANOFF, ROAST_DINNER,
            HONEY_GLAZED_HAM, SHEPHERDS_PIE, SPAGHETTI, FRIED_CHICKEN, CHICKEN_SATAY, KEBAB,
            FRIED_FISH, FISH_AND_CHIPS, SURF_N_TURF, EGG_FRIED_RICE, HAMBURGER, CHICKEN_SANDWICH,
            GRILLED_CHEESE, BLT, PB_AND_J, BURRITO, GYRO, LETTUCE_WRAP, POUTINE, SOURDOUGH_BREAD,
            COFFEE, HOT_CHOCOLATE, APPLE_CIDER, MARSHMALLOW, TOASTED_MARSHMALLOW, BROWNED_MARSHMALLOW,
            BURNT_MARSHMALLOW, VOLCANIC_MARSHMALLOW, WINE, GRAPE_WINE, FINE_WINE, SWEET_BERRY_WINE,
            MULLED_WINE, BLAZING_WINE, BREEZING_WINE, CREEPING_WINE, DIVING_WINE, GLEAMING_WINE,
            BLESSING_WINE, CHORUS_WINE, HEROIC_WINE, PROSPECTING_WINE,
    };

    static final Item[] INGREDIENTS = {
            CORN_SEEDS, RICE_SEEDS, TOMATO_SEEDS, LETTUCE_SEEDS, GRAPE_SEEDS, COFFEE_CHERRIES,
            COFFEE_BEANS, CINNAMON, DOUGH, MILK_BOTTLE,
    };
}
