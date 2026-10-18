package com.aura.lamplight.puppeteer.animation;

import com.aura.lamplight.puppeteer.Puppeteer;
import java.util.Collection;
import java.util.Map;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jspecify.annotations.Nullable;

public class AnimationDefinitions extends SimpleJsonResourceReloadListener<AnimationDefinition> implements IdentifiableResourceReloadListener {

    private static volatile Map<Identifier, AnimationDefinition> LOADED = Map.of();

    public AnimationDefinitions() {
        super(AnimationDefinition.CODEC, FileToIdConverter.json("animation"));
    }

    @Override
    protected void apply(final Map<Identifier, AnimationDefinition> data, final ResourceManager manager, final ProfilerFiller profiler) {
        LOADED = Map.copyOf(data);
        Puppeteer.LOGGER.info("Loaded {} animation definitions", LOADED.size());
    }

    @Override
    public Identifier getFabricId() {
        return Puppeteer.id("animations");
    }

    public static Collection<AnimationDefinition> all() {
        return LOADED.values();
    }

    public static @Nullable AnimationDefinition byId(final Identifier id) {
        return LOADED.get(id);
    }

    public static void initialize() {
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new AnimationDefinitions());
    }
}

