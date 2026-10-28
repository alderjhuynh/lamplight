package com.aura.lamplight.menagerie.client.wisp;

import com.aura.lamplight.menagerie.Menagerie;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

public final class WispRenderTypes {
    public static final RenderPipeline WISP_PIPELINE = createPipeline();

    public static final RenderType WISP = RenderType.create(
            Menagerie.id("wisp").toString(),
            RenderSetup.builder(WISP_PIPELINE).createRenderSetup());

    private WispRenderTypes() {
    }

    private static RenderPipeline createPipeline() {
        return RenderPipelines.register(RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
                .withLocation(Menagerie.id("pipeline/wisp"))
                .withVertexShader(Menagerie.id("core/wisp"))
                .withFragmentShader(Menagerie.id("core/wisp"))
                .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL)
                .withPrimitiveTopology(PrimitiveTopology.QUADS)
                .withCull(false)
                .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, true))
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .build());
    }

    public static void init() {
    }
}
