package com.aura.lamplight.ambrosia.client;

import com.aura.lamplight.ambrosia.block.CookingPotBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class CookingPotRenderer implements BlockEntityRenderer<CookingPotBlockEntity, CookingPotRenderer.PotState> {
    private static final float INNER_MIN = 3.0F / 16.0F;
    private static final float INNER_MAX = 13.0F / 16.0F;
    private static final float HALF_INNER = (INNER_MAX - INNER_MIN) * 0.5F; 
    private static final float SURFACE_Y = 14.0F / 16.0F;
    private static final float WALL_MARGIN = 0.01F;

    private static final float ITEM_SCALE = 0.375F;
    private static final float DRIFT_RADIUS = 0.09F;
    private static final float DRIFT_SPEED = 0.035F;
    private static final float SPIN_SPEED = 0.4F;
    private static final float GOLDEN_ANGLE = 137.5F;

    private static final float LIFT = 0.006F;
    private static final float LAYER_STEP = 0.02F;
    private static final float BOB_AMPLITUDE = 0.003F;
    private static final float BOB_SPEED = 0.12F;

    private final ItemModelResolver itemModelResolver;

    public CookingPotRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public PotState createRenderState() {
        return new PotState();
    }

    @Override
    public void extractRenderState(CookingPotBlockEntity pot, PotState state, float partialTick, Vec3 cameraPos,
                                   net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(pot, state, partialTick, cameraPos, crumbling);

        Level level = pot.getLevel();
        BlockPos pos = pot.getBlockPos();
        state.time = level == null ? 0.0D : level.getGameTime() + (double) partialTick;
        state.seed = pos.hashCode();
        state.phase = Math.floorMod(state.seed, 360);

        List<ItemStack> ingredients = pot.getIngredients();
        while (state.items.size() > ingredients.size()) {
            state.items.removeLast();
        }
        while (state.items.size() < ingredients.size()) {
            state.items.add(new IngredientState());
        }
        for (int i = 0; i < ingredients.size(); i++) {
            IngredientState ingredient = state.items.get(i);
            this.itemModelResolver.updateForTopItem(ingredient.render, ingredients.get(i),
                    ItemDisplayContext.FIXED, level, null, state.seed + i);
            ingredient.measure();
        }
    }

    @Override
    public void submit(PotState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        double driftBase = (state.time * DRIFT_SPEED) % (Math.PI * 2.0D);
        double spinBase = (state.time * SPIN_SPEED) % 360.0D;

        for (int i = 0; i < state.items.size(); i++) {
            IngredientState ingredient = state.items.get(i);
            if (ingredient.render.isEmpty()) {
                continue;
            }

            float phase = state.phase + i * GOLDEN_ANGLE;

            float scale = ITEM_SCALE;
            float radius = ingredient.modelRadius * scale;
            float limit = HALF_INNER - WALL_MARGIN;
            if (radius > limit) {
                scale *= limit / radius;
                radius = limit;
            }
            float drift = Math.min(DRIFT_RADIUS, limit - radius);

            float driftAngle = (float) (driftBase + Math.toRadians(phase));
            float x = 0.5F + Mth.cos(driftAngle) * drift;
            float z = 0.5F + Mth.sin(driftAngle) * drift;

            float halfThickness = ingredient.modelHalfThickness * scale;
            float rise = LIFT + (ingredient.flat ? halfThickness : halfThickness * 0.5F);
            float bob = Mth.sin((float) (state.time * BOB_SPEED) + phase) * BOB_AMPLITUDE;
            float y = SURFACE_Y + rise + i * LAYER_STEP + bob;

            pose.pushPose();
            pose.translate(x, y, z);
            pose.mulPose(Axis.YP.rotationDegrees((float) ((spinBase + phase) % 360.0D)));
            if (ingredient.flat) {
                pose.mulPose(Axis.XP.rotationDegrees(90.0F));
            }
            pose.scale(scale, scale, scale);
            ingredient.render.submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            pose.popPose();
        }
    }

    public static class PotState extends BlockEntityRenderState {
        public final List<IngredientState> items = new ArrayList<>();
        public double time;
        public float phase;
        public int seed;
    }

    public static class IngredientState {
        public final ItemStackRenderState render = new ItemStackRenderState();
        public boolean flat = true;
        public float modelRadius = 0.25F;
        public float modelHalfThickness = 0.016F;

        void measure() {
            if (this.render.isEmpty()) {
                return;
            }
            this.flat = !this.render.usesBlockLight();
            AABB box = this.render.getModelBoundingBox();
            if (box.getXsize() < 1.0E-4D || box.getYsize() < 1.0E-4D) {
                return;
            }
            float hx = (float) Math.max(Math.abs(box.minX), Math.abs(box.maxX));
            float hy = (float) Math.max(Math.abs(box.minY), Math.abs(box.maxY));
            float hz = (float) Math.max(Math.abs(box.minZ), Math.abs(box.maxZ));
            if (this.flat) {
                this.modelRadius = (float) Math.sqrt(hx * hx + hy * hy);
                this.modelHalfThickness = (float) box.getZsize() * 0.5F;
            } else {
                this.modelRadius = (float) Math.sqrt(hx * hx + hz * hz);
                this.modelHalfThickness = (float) box.getYsize() * 0.5F;
            }
        }
    }
}