package com.aura.lamplight.menagerie.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

@SuppressWarnings("all")
public class RaccoonModel extends EntityModel<RaccoonRenderState> {
    private final ModelPart body;
    private final ModelPart tail;
    private final ModelPart armLeft;
    private final ModelPart armRight;
    private final ModelPart legLeft;
    private final ModelPart legRight;
    private final ModelPart head;
    private final ModelPart earLeft;
    private final ModelPart earRight;
    private final ModelPart snout;

    public RaccoonModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.tail = body.getChild("tail");
        this.armLeft = body.getChild("arm_left");
        this.armRight = body.getChild("arm_right");
        this.legLeft = body.getChild("leg_left");
        this.legRight = body.getChild("leg_right");
        this.head = body.getChild("head");
        this.earLeft = head.getChild("ear_left");
        this.earRight = head.getChild("ear_right");
        this.snout = head.getChild("snout");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).addBox(-5.5F, -4.0F, -7.5F, 11.0F, 8.0F, 15.0F),
                PartPose.offsetAndRotation(0.0F, -11.0F, 0.5F, 0.0F, 0.0F, 0.0F));
        PartDefinition body = root.getChild("body");
        body.addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(0, 24).addBox(-3.0F, -2.0F, 0.0F, 5.0F, 5.0F, 19.0F),
                PartPose.offsetAndRotation(0.5F, -1.0F, 7.5F, 0.0F, 0.0F, 0.0F));
        body.addOrReplaceChild("arm_left",
                CubeListBuilder.create().texOffs(0, 24).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F),
                PartPose.offsetAndRotation(3.0F, 4.0F, -5.5F, 0.0F, 0.0F, 0.0F));
        body.addOrReplaceChild("arm_right",
                CubeListBuilder.create().texOffs(0, 24).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F),
                PartPose.offsetAndRotation(-3.0F, 4.0F, -5.5F, 0.0F, 0.0F, 0.0F));
        body.addOrReplaceChild("leg_left",
                CubeListBuilder.create().texOffs(9, 32).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F),
                PartPose.offsetAndRotation(3.0F, 4.0F, 6.5F, 0.0F, 0.0F, 0.0F));
        body.addOrReplaceChild("leg_right",
                CubeListBuilder.create().texOffs(9, 32).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F),
                PartPose.offsetAndRotation(-3.0F, 4.0F, 6.5F, 0.0F, 0.0F, 0.0F));
        PartDefinition head = body.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(30, 30).addBox(-4.5F, -4.0F, -4.0F, 9.0F, 7.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 0.5F, -8.5F, 0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("ear_left",
                CubeListBuilder.create().texOffs(9, 24).addBox(-1.0F, -2.0F, 0.0F, 2.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(3.5F, -4.0F, -2.0F, 0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("ear_right",
                CubeListBuilder.create().texOffs(9, 24).mirror().addBox(-1.0F, -2.0F, 0.0F, 2.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(-3.5F, -4.0F, -2.0F, 0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("snout",
                CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -1.5F, -2.0F, 4.0F, 3.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 1.5F, -5.0F, 0.0F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    public ModelPart body() {
        return body;
    }

    public ModelPart armRight() {
        return armRight;
    }

    public ModelPart head() {
        return head;
    }

    @Override
    public void setupAnim(RaccoonRenderState state) {
        resetToDefaultPose();
        float age = state.ageInTicks;
        float walkPos = state.walkAnimationPos;
        float walkAmount = Math.min(state.walkAnimationSpeed, 1.0F);
        float standProgress = state.standProgress;
        float washProgress = state.washProgress;
        float runProgress = 5F * walkAmount;

        // standing up on hind legs
        rotateProgress(body, standProgress, -70, 0, 0);
        rotateProgress(armLeft, standProgress, 70, 0, 0);
        rotateProgress(armRight, standProgress, 70, 0, 0);
        rotateProgress(legLeft, standProgress, 70, 0, 0);
        rotateProgress(legRight, standProgress, 70, 0, 0);
        rotateProgress(head, standProgress, 70, 0, 0);

        // standing pose adjustments.
        moveProgress(head, standProgress, 0, -2.0F, 0);
        moveProgress(body, standProgress, 0, -3.0F, 0);
        moveProgress(legLeft, standProgress, 0, -2.0F, 0);
        moveProgress(legRight, standProgress, 0, -2.0F, 0);
        moveProgress(armLeft, standProgress, 0, 1.0F, 0);
        moveProgress(armRight, standProgress, 0, 1.0F, 0);
        rotateProgress(tail, standProgress, 80, 0, 0);

        // eating
        if (state.eatProgress > 0.0F) {
            rotateProgress(armRight, state.eatProgress,-100, 0, -10);
            rotateProgress(armLeft, state.eatProgress, -100, 0, 10);
            float chew = Mth.cos(age * 0.4F) * 0.2F * clampProgress(state.eatProgress);
            armRight.xRot += chew;
            armLeft.xRot += chew * 0.7F;
            // Slight nod as the raccoon nibbles, synced with the paw bob.
            head.xRot += chew * 0.25F;
        }

        // idle
        rotateProgress(body, 5, 10, 0, 0);
        rotateProgress(head, 5, -10, 0, 0);
        rotateProgress(armLeft, 5, -10, 0, 0);
        rotateProgress(armRight, 5, -10, 0, 0);
        rotateProgress(legLeft, 5, -10, 0, 0);
        rotateProgress(legRight, 5, -10, 0, 0);
        moveProgress(body, 5, 0, 1.0F, 0);
        moveProgress(armLeft, 5, 0, -1.9F, 0);
        moveProgress(armRight, 5, 0, -1.9F, 0);
        moveProgress(legLeft, 5, 0, 0.0F, -1);
        moveProgress(legRight, 5, 0, 0.0F, -1);

        // tail
        rotateProgress(tail, 5.0F - runProgress, -35, 0, 0);
        rotateProgress(tail, runProgress, -10, 0, 0);
        rotateProgress(earLeft, runProgress, -20, 0, 20);
        rotateProgress(earRight, runProgress, -20, 0, -20);

        // washing
        rotateProgress(body, washProgress, 15, 0, 0);
        rotateProgress(armLeft, washProgress, -90, 0, 0);
        rotateProgress(armRight, washProgress, -90, 0, 0);
        rotateProgress(legLeft, washProgress, -15, 0, 0);
        rotateProgress(legRight, washProgress, -15, 0, 0);
        rotateProgress(tail, washProgress, -15, 0, 0);
        rotateProgress(head, washProgress, 15, 0, 0);
        moveProgress(head, washProgress, 0, -3.0F, 0.4F);
        moveProgress(body, washProgress, 0, 1.5F, -10);
        moveProgress(armLeft, washProgress, 0, 1.0F, -1.4F);
        moveProgress(armRight, washProgress, 0, 1.0F, -1.4F);
        if (washProgress > 0) {
            armLeft.y -= (float) (-Math.abs(Math.sin(age * 0.5F) * washProgress * 0.2D));
            armLeft.z -= (float) (-Math.abs(Math.sin(age * 0.25F) * washProgress * 0.2D * 3));
            armRight.y -= (float) (-Math.abs(Math.sin(age * 0.5F) * washProgress * 0.2D));
            armRight.z -= (float) (-Math.abs(Math.cos(age * 0.25F) * washProgress * 0.2D * 3));
            swing(armRight, 0.5F, 0.25F, false, 2F, -0.1F, age, washProgress * 0.2F);
            swing(armLeft, 0.5F, 0.25F, true, 2F, -0.1F, age, washProgress * 0.2F);
            float bodyFlap = (float) (Math.sin(age * 0.5F) * washProgress * 0.2D * 0.150);
            body.zRot += bodyFlap;
            tail.yRot += bodyFlap;
            head.zRot -= bodyFlap;
            legLeft.zRot -= bodyFlap;
            legRight.zRot -= bodyFlap;
        }

        if (standProgress <= 0.0F) {
            walk(armRight, 1.0F, 0.8F * 1.1F, true, 0F, 0F, walkPos, walkAmount);
            walk(armLeft, 1.0F, 0.8F * 1.1F, false, 0F, 0F, walkPos, walkAmount);
        }
        swing(tail, 0.1F, 0.2F, false, 2F, 0.0F, age, 1.0F);
        swing(body, 1.0F, 0.8F * 0.3F, false, 3F, 0.0F, walkPos, walkAmount);
        swing(tail, 1.0F, 0.8F, false, 4F, 0.0F, walkPos, walkAmount);
        walk(legRight, 1.0F, 0.8F * 1.1F, false, 0F, 0.0F, walkPos, walkAmount);
        walk(legLeft, 1.0F, 0.8F * 1.1F, true, 0F, 0.0F, walkPos, walkAmount);

        if (washProgress <= 0.01F) {
            head.yRot += (float) Math.toRadians(state.yRot) / 1.3F;
            head.xRot += (float) Math.toRadians(state.xRot) / 1.3F;
        }

        applyAttackAnimation(state);

        if (state.isBaby) {
            head.xScale = 1.65F;
            head.yScale = 1.65F;
            head.zScale = 1.65F;
        }
    }

    private void applyAttackAnimation(RaccoonRenderState state) {
        if (state.attackTick <= 0) {
            return;
        }
        float elapsed = (12 - state.attackTick);
        float headRotate = -20;
        float r;
        float l;
        if (elapsed < 3) {
            float f = clamp01(elapsed / 3.0F);
            r = lerp(-120, -120, f);
            l = lerp(-40, -40, f);
        } else if (elapsed < 6) {
            float f = clamp01((elapsed - 3) / 3.0F);
            r = lerp(-120, -40, f);
            l = lerp(-40, -120, f);
        } else if (elapsed < 9) {
            float f = clamp01((elapsed - 6) / 3.0F);
            r = lerp(-40, -120, f);
            l = lerp(-120, -40, f);
        } else {
            float f = clamp01((elapsed - 9) / 3.0F);
            r = lerp(-120, -120, f);
            l = lerp(-40, -40, f);
        }
        float f2 = elapsed < 9 ? clamp01(elapsed / 3.0F) - clamp01(Math.max(elapsed - 9, 0) / 3.0F) : 1.0F - clamp01((elapsed - 9) / 3.0F);
        head.xRot += Math.toRadians(headRotate) * Math.max(f2, 0.3F);
        armRight.xRot += Math.toRadians(r);
        armRight.zRot += Math.toRadians(30);
        armLeft.xRot += Math.toRadians(l);
        armLeft.zRot += Math.toRadians(-20);
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private static float clamp01(float f) {
        return f < 0.0F ? 0.0F : (f > 1.0F ? 1.0F : f);
    }

    private void resetToDefaultPose() {
        resetPart(root, 0.0F, 24.0F, 0.0F);
        resetPart(body, 0.0F, -11.0F, 0.5F);
        resetPart(tail, 0.5F, -1.0F, 7.5F);
        resetPart(armLeft, 3.0F, 4.0F, -5.5F);
        resetPart(armRight, -3.0F, 4.0F, -5.5F);
        resetPart(legLeft, 3.0F, 4.0F, 6.5F);
        resetPart(legRight, -3.0F, 4.0F, 6.5F);
        resetPart(head, 0.0F, 0.5F, -8.5F);
        resetPart(earLeft, 3.5F, -4.0F, -2.0F);
        resetPart(earRight, -3.5F, -4.0F, -2.0F);
        resetPart(snout, 0.0F, 1.5F, -5.0F);
    }

    private static void resetPart(ModelPart part, float x, float y, float z) {
        part.x = x;
        part.y = y;
        part.z = z;
        part.xRot = 0.0F;
        part.yRot = 0.0F;
        part.zRot = 0.0F;
        part.xScale = 1.0F;
        part.yScale = 1.0F;
        part.zScale = 1.0F;
    }

    private static void rotateProgress(ModelPart part, float progress, float degX, float degY, float degZ) {
        float f = clampProgress(progress);
        part.xRot += (float) Math.toRadians(degX) * f;
        part.yRot += (float) Math.toRadians(degY) * f;
        part.zRot += (float) Math.toRadians(degZ) * f;
    }

    private static void moveProgress(ModelPart part, float progress, float px, float py, float pz) {
        float f = clampProgress(progress);
        part.x += px * f;
        part.y += py * f;
        part.z += pz * f;
    }

    private static float clampProgress(float progress) {
        float f = progress / 5.0F;
        if (f < 0.0F) f = 0.0F;
        if (f > 1.0F) f = 1.0F;
        return f;
    }

    private static void walk(ModelPart part, float speed, float degree, boolean invert, float offset, float weight, float stride, float amount) {
        part.xRot += calculateRotation(speed, degree, invert, offset, weight, stride, amount);
    }

    private static void swing(ModelPart part, float speed, float degree, boolean invert, float offset, float weight, float stride, float amount) {
        part.yRot += calculateRotation(speed, degree, invert, offset, weight, stride, amount);
    }

    private static void flap(ModelPart part, float speed, float degree, boolean invert, float offset, float weight, float stride, float amount) {
        part.zRot += calculateRotation(speed, degree, invert, offset, weight, stride, amount);
    }

    private static float calculateRotation(float speed, float degree, boolean invert, float offset, float weight, float stride, float amount) {
        float rotation = Mth.cos(stride * speed + offset) * degree * amount + weight * amount;
        return invert ? -rotation : rotation;
    }
}
