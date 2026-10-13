package com.aura.picturesque.client.raytrace.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameRenderer.class)
public interface GameRendererAccessor {
    @Accessor("resourcePool")
    CrossFrameResourcePool picturesque$getResourcePool();

    @Accessor("mainRenderTarget")
    RenderTarget picturesque$getMainRenderTarget();
}
