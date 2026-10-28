package com.aura.lamplight.menagerie.client;

import com.aura.lamplight.menagerie.Menagerie;
import com.aura.lamplight.menagerie.raccoon.RaccoonEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class RaccoonRenderer extends MobRenderer<RaccoonEntity, RaccoonRenderState, RaccoonModel> {
    private static final Identifier TEXTURE = Menagerie.id("textures/entity/raccoon.png");

    public RaccoonRenderer(EntityRendererProvider.Context context) {
        super(context, new RaccoonModel(context.bakeLayer(RaccoonModelLayers.MODEL_LAYER)), 0.4F);
        this.addLayer(new RaccoonHeldItemLayer(this));
    }

    @Override
    public RaccoonRenderState createRenderState() {
        return new RaccoonRenderState();
    }

    @Override
    public void extractRenderState(RaccoonEntity entity, RaccoonRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.raccoon = entity;
        state.standProgress = lerpProgress(entity.prevStandProgress, entity.standProgress, partialTick);
        state.washProgress = lerpProgress(entity.prevWashProgress, entity.washProgress, partialTick);
        state.eatProgress = lerpProgress(entity.prevEatProgress, entity.eatProgress, partialTick);
        state.attackTick = entity.getAttackTick();
        state.heldItem = entity.getMainHandItem().copy();
    }

    private static float lerpProgress(float prev, float current, float partialTick) {
        return prev + (current - prev) * partialTick;
    }

    @Override
    protected void scale(RaccoonRenderState state, com.mojang.blaze3d.vertex.PoseStack poseStack) {
        float f = state.isBaby ? 0.375F : 0.75F;
        poseStack.scale(f, f, f);
    }

    @Override
    public Identifier getTextureLocation(RaccoonRenderState state) {
        return TEXTURE;
    }
}
