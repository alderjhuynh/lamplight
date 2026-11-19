package com.aura.lamplight.folio.client;

import com.aura.lamplight.folio.Folio;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

public final class DragonbladeGrayscaleRenderTypes {
    private DragonbladeGrayscaleRenderTypes() {}

    private static RenderPipeline pipeline;
    private static final Map<Identifier, RenderType> CACHE = new HashMap<>();

    public static RenderType get(Identifier texture) {
        return CACHE.computeIfAbsent(texture, t -> {
            if (pipeline == null) pipeline = createPipeline();
            RenderSetup setup = RenderSetup.builder(pipeline)
                    .withTexture("Sampler0", t)
                    .useLightmap()
                    .useOverlay()
                    .createRenderSetup();
            return RenderType.create(Folio.id("dragonblade_grayscale").toString() + "_" + t, setup);
        });
    }

    private static RenderPipeline createPipeline() {
        try {
            Field f = RenderPipelines.class.getDeclaredField("ENTITY_SNIPPET");
            f.setAccessible(true);
            RenderPipeline.Snippet snippet = (RenderPipeline.Snippet) f.get(null);
            return RenderPipelines.register(RenderPipeline.builder(snippet)
                    .withLocation(Folio.id("pipeline/dragonblade_grayscale"))
                    .withFragmentShader(Folio.id("core/dragonblade_grayscale"))
                    .withShaderDefine("ALPHA_CUTOUT", 0.1f)
                    .withShaderDefine("PER_FACE_LIGHTING")
                    .withBindGroupLayout(BindGroupLayouts.SAMPLER1)
                    .withCull(false)
                    .build());
        } catch (Throwable t) {
            throw new RuntimeException("Failed to create grayscale pipeline", t);
        }
    }
}
