package com.aura.lamplight.ambrosia.food;

import com.aura.lamplight.ambrosia.Ambrosia;
import com.aura.lamplight.ambrosia.effect.ModEffects;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ConsumeEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class VanillaFoodTweaks {
    private VanillaFoodTweaks() {
    }

    public static final int NO_EFFECT = 0;
    public static final int ONE_MINUTE = 60 * 20;
    public static final int THREE_MINUTES = 3 * 60 * 20;
    public static final int ONE_AND_A_HALF_MINUTES = THREE_MINUTES / 2;
    public static final int THIRTY_SECONDS = ONE_MINUTE / 2;
    public static final int FIVE_MINUTES = 5 * 60 * 20;

    public static final int DEFAULT_FAMISHED_TICKS = ONE_MINUTE;
    public static final int DEFAULT_NOURISHMENT_TICKS = FIVE_MINUTES;
    public static final int DEFAULT_COMFORT_TICKS = ONE_MINUTE;

    private static final Map<Item, VanillaFoodOverride> OVERRIDES = new ConcurrentHashMap<>();
    private static final Map<Item, CacheEntry> PATCH_CACHE = new ConcurrentHashMap<>();

    private record CacheEntry(DataComponentMap original, DataComponentMap patched, VanillaFoodOverride override) {
    }

    public static void register(Item item, int hunger, float saturation) {
        register(item, hunger, saturation, NO_EFFECT, NO_EFFECT);
    }

    public static void register(Item item, int hunger, int saturation) {
        register(item, hunger, (float) saturation);
    }

    public static void register(Item item, int hunger, float saturation, int famishedTicks) {
        register(item, hunger, saturation, famishedTicks, NO_EFFECT);
    }

    public static void register(Item item, int hunger, int saturation, int famishedTicks) {
        register(item, hunger, (float) saturation, famishedTicks, NO_EFFECT);
    }

    public static void register(Item item, int hunger, float saturation, int famishedTicks, int nourishmentTicks) {
        register(item, hunger, saturation, famishedTicks, nourishmentTicks, NO_EFFECT);
    }

    public static void register(Item item, int hunger, int saturation, int famishedTicks, int nourishmentTicks) {
        register(item, hunger, (float) saturation, famishedTicks, nourishmentTicks);
    }

    public static void register(Item item, int hunger, float saturation, int famishedTicks, int nourishmentTicks, int comfortTicks) {
        if (hunger < 0) {
            throw new IllegalArgumentException("hunger must be >= 0, got " + hunger);
        }
        if (saturation < 0) {
            throw new IllegalArgumentException("saturation must be >= 0, got " + saturation);
        }
        tweak(item, new VanillaFoodOverride(
                java.util.Optional.of(hunger),
                java.util.Optional.of(saturation),
                famishedTicks > 0 ? java.util.Optional.of(famishedTicks) : java.util.Optional.empty(),
                nourishmentTicks > 0 ? java.util.Optional.of(nourishmentTicks) : java.util.Optional.empty(),
                comfortTicks > 0 ? java.util.Optional.of(comfortTicks) : java.util.Optional.empty()
        ));
    }

    public static void register(Item item, int hunger, int saturation, int famishedTicks, int nourishmentTicks, int comfortTicks) {
        register(item, hunger, (float) saturation, famishedTicks, nourishmentTicks, comfortTicks);
    }

    public static void tweak(Item item, String json) {
        tweak(item, JsonParser.parseString(json).getAsJsonObject());
    }

    public static void tweak(Item item, JsonObject json) {
        tweak(item, VanillaFoodOverride.fromJson(json));
    }

    public static void tweak(Item item, VanillaFoodOverride override) {
        if (override.isEmpty()) {
            OVERRIDES.remove(item);
        } else {
            OVERRIDES.put(item, override);
        }
        PATCH_CACHE.remove(item);
    }
    
    public static VanillaFoodOverride get(Item item) {
        return OVERRIDES.get(item);
    }

    public static FoodProperties tweakedFood(ItemStack stack) {
        FoodProperties current = stack.get(DataComponents.FOOD);
        VanillaFoodOverride override = OVERRIDES.get(stack.getItem());
        if (override == null || !override.hasFoodValues()) {
            return current;
        }
        return resolveFood(current, override);
    }

    public static Consumable tweakedConsumable(ItemStack stack) {
        Consumable current = stack.get(DataComponents.CONSUMABLE);
        VanillaFoodOverride override = OVERRIDES.get(stack.getItem());
        if (override == null || !override.hasEffects()) {
            return current;
        }
        return resolveConsumable(current, override);
    }

    public static DataComponentMap patch(Item item, DataComponentMap original) {
        VanillaFoodOverride override = OVERRIDES.get(item);
        if (override == null) {
            PATCH_CACHE.remove(item);
            return original;
        }

        CacheEntry cached = PATCH_CACHE.get(item);
        if (cached != null && cached.original() == original && cached.override().equals(override)) {
            return cached.patched();
        }

        DataComponentMap patched = buildPatched(item, original, override);
        PATCH_CACHE.put(item, new CacheEntry(original, patched, override));
        return patched;
    }

    private static DataComponentMap buildPatched(Item item, DataComponentMap original, VanillaFoodOverride override) {
        FoodProperties food = original.get(DataComponents.FOOD);
        Consumable consumable = original.get(DataComponents.CONSUMABLE);

        FoodProperties newFood = resolveFood(food, override);
        Consumable newConsumable = resolveConsumable(consumable, override);

        if (newFood == food && newConsumable == consumable) {
            return original;
        }

        DataComponentMap.Builder builder = DataComponentMap.builder().addAll(original);
        if (newFood != null && newFood != food) {
            builder.set(DataComponents.FOOD, newFood);
        }
        if (newConsumable != null && newConsumable != consumable) {
            builder.set(DataComponents.CONSUMABLE, newConsumable);
        }
        DataComponentMap patched = builder.build();
        Ambrosia.LOGGER.debug("Applied food tweak to {}: {}", item, override);
        return patched;
    }

    static FoodProperties resolveFood(FoodProperties original, VanillaFoodOverride override) {
        if (!override.hasFoodValues()) {
            return original;
        }
        if (original == null) {
            return new FoodProperties(
                    override.nutrition().orElse(0),
                    override.saturation().orElse(0.0F),
                    false
            );
        }
        int nutrition = override.nutrition().orElse(original.nutrition());
        float saturation = override.saturation().orElse(original.saturation());
        if (nutrition == original.nutrition()
                && Float.compare(saturation, original.saturation()) == 0) {
            return original;
        }
        return new FoodProperties(nutrition, saturation, original.canAlwaysEat());
    }

    static Consumable resolveConsumable(Consumable original, VanillaFoodOverride override) {
        if (!override.hasEffects()) {
            return original;
        }
        if (original == null) {
            Ambrosia.LOGGER.warn("Cannot add eat effects: item has no CONSUMABLE component");
            return null;
        }

        List<ConsumeEffect> effects = new ArrayList<>(original.onConsumeEffects());
        effects.removeIf(effect -> effect instanceof ApplyStatusEffectsConsumeEffect apply
                && apply.effects().stream().anyMatch(VanillaFoodTweaks::isAmbrosiaStewEffect));

        override.famishedTicks().ifPresent(ticks ->
                effects.add(new ApplyStatusEffectsConsumeEffect(
                        ModEffects.famished(ticks), 1.0F)));
        override.nourishmentTicks().ifPresent(ticks ->
                effects.add(new ApplyStatusEffectsConsumeEffect(
                        ModEffects.nourishment(ticks), 1.0F)));
        override.comfortTicks().ifPresent(ticks ->
                effects.add(new ApplyStatusEffectsConsumeEffect(
                        ModEffects.comfort(ticks), 1.0F)));

        Consumable.Builder replacement = Consumable.builder()
                .consumeSeconds(original.consumeSeconds())
                .animation(original.animation())
                .sound(original.sound())
                .hasConsumeParticles(original.hasConsumeParticles());
        for (ConsumeEffect effect : effects) {
            replacement.onConsume(effect);
        }
        return replacement.build();
    }

    private static boolean isAmbrosiaStewEffect(MobEffectInstance instance) {
        return isHolder(instance.getEffect(), ModEffects.FAMISHED)
                || isHolder(instance.getEffect(), ModEffects.NOURISHMENT)
                || isHolder(instance.getEffect(), ModEffects.COMFORT);
    }

    private static boolean isHolder(Holder<MobEffect> a, Holder<MobEffect> b) {
        return a.value() == b.value();
    }
}
