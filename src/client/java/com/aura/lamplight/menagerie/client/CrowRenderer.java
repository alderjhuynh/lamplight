package com.aura.lamplight.menagerie.client;

import com.aura.lamplight.menagerie.Menagerie;
import com.aura.lamplight.menagerie.crow.CrowEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class CrowRenderer extends MobRenderer<CrowEntity, CrowRenderState, CrowModel> {
    private static final Identifier TEXTURE = Menagerie.id("textures/entity/crow.png");

    public CrowRenderer(EntityRendererProvider.Context context) {
        super(context, new CrowModel(context.bakeLayer(CrowModelLayers.MODEL_LAYER)), 0.2F);
        addLayer(new CrowHeldItemLayer(this));
    }

    @Override
    public CrowRenderState createRenderState() {
        return new CrowRenderState();
    }

    @Override
    public void extractRenderState(CrowEntity entity, CrowRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.crow = entity;
        state.flyProgress = (entity.prevFlyProgress + (entity.flyProgress - entity.prevFlyProgress) * partialTick) / 5F;
        state.sitProgress = (entity.prevSitProgress + (entity.sitProgress - entity.prevSitProgress) * partialTick) / 5F;
        state.attackProgress = (entity.prevAttackProgress + (entity.attackProgress - entity.prevAttackProgress) * partialTick) / 5F;
        state.heldItem = entity.getMainHandItem().copy();
    }

    @Override
    public Identifier getTextureLocation(CrowRenderState state) {
        return TEXTURE;
    }
}
