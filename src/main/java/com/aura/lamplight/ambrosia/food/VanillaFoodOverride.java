package com.aura.lamplight.ambrosia.food;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Optional;

public record VanillaFoodOverride(
        Optional<Integer> nutrition,
        Optional<Float> saturation,
        Optional<Integer> famishedTicks,
        Optional<Integer> nourishmentTicks,
        Optional<Integer> comfortTicks
) {
    public static VanillaFoodOverride empty() {
        return new VanillaFoodOverride(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    public static VanillaFoodOverride fromJson(String json) {
        return fromJson(JsonParser.parseString(json).getAsJsonObject());
    }

    public static VanillaFoodOverride fromJson(JsonObject json) {
        return new VanillaFoodOverride(
                readNutrition(json),
                readSaturation(json),
                readEffectTicks(json, VanillaFoodTweaks.DEFAULT_FAMISHED_TICKS,
                        "famishedTicks", "famished"),
                readEffectTicks(json, VanillaFoodTweaks.DEFAULT_NOURISHMENT_TICKS,
                        "nourishmentTicks", "nourishment", "nourished"),
                readEffectTicks(json, VanillaFoodTweaks.DEFAULT_COMFORT_TICKS,
                        "comfortTicks", "comfort")
        );
    }

    public boolean hasFoodValues() {
        return nutrition.isPresent() || saturation.isPresent();
    }

    public boolean hasEffects() {
        return famishedTicks.isPresent() || nourishmentTicks.isPresent() || comfortTicks.isPresent();
    }

    public boolean isEmpty() {
        return !hasFoodValues() && !hasEffects();
    }

    private static Optional<Integer> readNutrition(JsonObject json) {
        JsonElement element = firstPresent(json, "nutrition", "food", "hunger", "foodLevel");
        if (isMissing(element)) {
            return Optional.empty();
        }
        int value = element.getAsInt();
        if (value < 0) {
            throw new IllegalArgumentException("nutrition must be >= 0, got " + value);
        }
        return Optional.of(value);
    }

    private static Optional<Float> readSaturation(JsonObject json) {
        JsonElement element = firstPresent(json, "saturation", "saturationModifier");
        if (isMissing(element)) {
            return Optional.empty();
        }
        float value = element.getAsFloat();
        if (value < 0) {
            throw new IllegalArgumentException("saturation must be >= 0, got " + value);
        }
        return Optional.of(value);
    }

    private static Optional<Integer> readEffectTicks(JsonObject json, int defaultTicks, String... names) {
        JsonElement element = firstPresent(json, names);
        if (isMissing(element)) {
            return Optional.empty();
        }
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean()) {
            return element.getAsBoolean() ? Optional.of(defaultTicks) : Optional.empty();
        }
        int ticks = element.getAsInt();
        return ticks > 0 ? Optional.of(ticks) : Optional.empty();
    }

    private static JsonElement firstPresent(JsonObject json, String... names) {
        for (String name : names) {
            JsonElement element = json.get(name);
            if (element != null) {
                return element;
            }
        }
        return null;
    }

    private static boolean isMissing(JsonElement element) {
        return element == null || element.isJsonNull();
    }
}
