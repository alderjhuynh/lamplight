package com.aura.lamplight.armory.client;

import com.aura.lamplight.armory.ThrownChakram;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;

public class ThrownChakramRenderer extends EntityRenderer<ThrownChakram, ThrownChakramRenderState> {
	private static final float SPIN_OUT = 55.0F;
	private static final float SPIN_BACK = 80.0F;
	private static final float SCALE = 0.85F;

	private final ItemModelResolver itemModelResolver;

	public ThrownChakramRenderer(final EntityRendererProvider.Context context) {
		super(context);
		this.itemModelResolver = context.getItemModelResolver();
	}

	@Override
	public ThrownChakramRenderState createRenderState() {
		return new ThrownChakramRenderState();
	}

	@Override
	public void extractRenderState(final ThrownChakram entity, final ThrownChakramRenderState state, final float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.returning = entity.isReturning();
		this.itemModelResolver.updateForNonLiving(state.item, entity.getItem(), ItemDisplayContext.NONE, entity);
	}

	@Override
	public void submit(final ThrownChakramRenderState state, final PoseStack poseStack,
			final SubmitNodeCollector collector, final CameraRenderState camera) {
		poseStack.pushPose();
		poseStack.translate(0.0F, 0.25F, 0.0F);
		poseStack.mulPose(Axis.YP.rotationDegrees(state.ageInTicks * (state.returning ? SPIN_BACK : SPIN_OUT)));
		poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
		poseStack.scale(SCALE, SCALE, SCALE);
		state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
		poseStack.popPose();
		super.submit(state, poseStack, collector, camera);
	}
}