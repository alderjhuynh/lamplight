package com.aura.lamplight;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aura.lamplight.puppeteer.animation.AnimationDefinition;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.server.Bootstrap;
import net.minecraft.SharedConstants;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** parse validation over shipped animation definition tree */
public final class AnimationDefinitionParseTest {

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    private static Path resources() {
        String root = System.getProperty("lamplight.root");
        Path base = root != null ? Path.of(root) : Path.of("").toAbsolutePath();
        return base.resolve("src").resolve("main").resolve("resources");
    }

    private static Path treeDir(final String tree) {
        return resources().resolve(tree).resolve("lamplight").resolve("animation");
    }

    private static Map<String, String> tree(final String tree) {
        Path dir = treeDir(tree);
        Map<String, String> files = new HashMap<>();
        try (java.util.stream.Stream<Path> listing = Files.list(dir)) {
            listing.forEach(path -> {
                try {
                    files.put(path.getFileName().toString(), Files.readString(path));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return files;
    }

    private static List<String> parseAll(final Map<String, String> files) {
        List<String> problems = new ArrayList<>();
        for (Map.Entry<String, String> entry : files.entrySet()) {
            AnimationDefinition.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(entry.getValue()))
                .ifError(error -> problems.add(entry.getKey() + ": " + error.message()));
            String filename = entry.getKey().replaceFirst("\\.json$", "");
            String embedded;
            try {
                embedded = AnimationDefinition.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(entry.getValue()))
                    .getOrThrow().id().toString();
            } catch (Exception e) {
                embedded = "<unparseable>";
            }
            if (!embedded.equals("lamplight:" + filename)) {
                problems.add(entry.getKey() + ": embedded id " + embedded
                    + " does not match filename lamplight:" + filename);
            }
        }
        return problems;
    }

    @Test
    void dataTreeParses() {
        List<String> problems = parseAll(tree("data"));
        assertTrue(problems.isEmpty(), () -> "unparseable definitions: " + problems);
    }

    @Test
    void assetsTreeParses() {
        List<String> problems = parseAll(tree("assets"));
        assertTrue(problems.isEmpty(), () -> "unparseable definitions: " + problems);
    }

    @Test
    void dataAndAssetsTreesMatch() {
        Map<String, String> data = tree("data");
        Map<String, String> assets = tree("assets");
        assertEquals(data.keySet(), assets.keySet(), "the data and assets animation trees must contain identical files");
        assertEquals(data, assets, "the data and assets animation trees must have identical contents");
    }

    @Test
    void conditionJsonKeysMatchCodecs() {
        Map<String, List<String>> allowed = new HashMap<>();
        allowed.put("all_of", List.of("conditions"));
        allowed.put("any_of", List.of("conditions"));
        allowed.put("not", List.of("condition"));
        allowed.put("flag", List.of("flag", "match"));
        allowed.put("speed", List.of("min", "max"));
        allowed.put("fall_distance", List.of("min", "max"));
        allowed.put("view_pitch", List.of("min", "max"));
        allowed.put("biome_temperature", List.of("min", "max"));
        allowed.put("motion_direction", List.of("direction"));
        allowed.put("held_item_group", List.of("group", "hand"));
        allowed.put("entity_pose", List.of("pose", "match"));
        allowed.put("has_mob_effect", List.of("effect", "min_amplifier"));
        allowed.put("passenger", List.of("entity", "match"));
        allowed.put("attribute_compare", List.of("attribute", "operator", "value"));
        allowed.put("has_item_component", List.of("hand", "component", "value"));
        allowed.put("attack_parity", List.of("side", "match"));
        allowed.put("manual", List.of());

        List<String> failures = new ArrayList<>();
        for (Map.Entry<String, String> entry : tree("data").entrySet()) {
            new SchemaAudit(allowed, failures).scan(JsonParser.parseString(entry.getValue()));
        }
        assertTrue(failures.isEmpty(), () -> "condition schema violations: " + failures);
    }

    private static final class SchemaAudit {

        private final Map<String, List<String>> allowed;
        private final List<String> failures;

        SchemaAudit(final Map<String, List<String>> allowed, final List<String> failures) {
            this.allowed = allowed;
            this.failures = failures;
        }

        void scan(final JsonElement element) {
            if (element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();
                if (object.has("type")) {
                    String type = object.get("type").getAsString();
                    if (type.startsWith("lamplight:")) {
                        String key = type.substring("lamplight:".length());
                        List<String> permitted = allowed.get(key);
                        if (permitted == null) {
                            failures.add("unknown condition type: " + key);
                        } else {
                            for (String field : object.keySet()) {
                                if (field.equals("type")) {
                                    continue;
                                }
                                if (!permitted.contains(field)) {
                                    failures.add("condition " + key + " has unknown key '" + field
                                        + "' (allowed: " + permitted + ")");
                                }
                            }
                        }
                    }
                }
                for (JsonElement value : object.asMap().values()) {
                    scan(value);
                }
            } else if (element.isJsonArray()) {
                for (JsonElement value : element.getAsJsonArray()) {
                    scan(value);
                }
            }
        }
    }
}
