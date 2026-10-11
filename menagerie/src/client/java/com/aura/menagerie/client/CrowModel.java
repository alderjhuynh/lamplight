package com.aura.menagerie.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class CrowModel extends EntityModel<CrowRenderState> {
    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart legLeft;
    private final ModelPart legRight;
    private final ModelPart wingLeft;
    private final ModelPart wingRight;
    private final ModelPart tail;
    private final ModelPart head;
    private final ModelPart beak;

    public CrowModel(ModelPart root) {
        super(root);
        this.root = root;
        this.body = root.getChild("body");
        this.legLeft = body.getChild("leg_left");
        this.legRight = body.getChild("leg_right");
        this.wingLeft = body.getChild("wing_left");
        this.wingRight = body.getChild("wing_right");
        this.tail = body.getChild("tail");
        this.head = body.getChild("head");
        this.beak = head.getChild("beak");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -5.0F, 0.0F, 3.0F, 5.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 21.9F, 0.0F, 1.0036F, 0.0F, 0.0F));
        body.addOrReplaceChild("leg_left",
                CubeListBuilder.create().texOffs(0, 17).addBox(-0.5F, -2.0F, -2.0F, 1.0F, 2.0F, 3.0F),
                PartPose.offsetAndRotation(0.9F, 0.0F, 0.0F, 0.5672F, 0.0F, 0.0F));
        body.addOrReplaceChild("leg_right",
                CubeListBuilder.create().texOffs(0, 17).mirror().addBox(-0.5F, -2.0F, -2.0F, 1.0F, 2.0F, 3.0F),
                PartPose.offsetAndRotation(-0.9F, 0.0F, 0.0F, 0.5672F, 0.0F, 0.0F));
        body.addOrReplaceChild("wing_left",
                CubeListBuilder.create().texOffs(13, 13).addBox(-0.5F, 0.0F, -1.7F, 1.0F, 6.0F, 3.0F),
                PartPose.offsetAndRotation(1.5F, -4.9F, 1.7F, 0.0436F, 0.0F, 0.0F));
        body.addOrReplaceChild("wing_right",
                CubeListBuilder.create().texOffs(13, 13).mirror().addBox(-0.5F, 0.0F, -1.7F, 1.0F, 6.0F, 3.0F),
                PartPose.offsetAndRotation(-1.5F, -4.9F, 1.7F, 0.0436F, 0.0F, 0.0F));
        body.addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(13, 0).addBox(-1.5F, 0.0F, -2.0F, 3.0F, 4.0F, 2.0F, new CubeDeformation(-0.1F)),
                PartPose.offsetAndRotation(0.0F, -0.1F, 3.0F, -0.1309F, 0.0F, 0.0F));
        PartDefinition head = body.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 9).addBox(-1.5F, -2.8F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(-0.2F)),
                PartPose.offsetAndRotation(0.0F, -4.8F, 1.7F, -0.7418F, 0.0F, 0.0F));
        head.addOrReplaceChild("beak",
                CubeListBuilder.create().texOffs(13, 7).addBox(-0.5F, -1.0F, -1.8F, 1.0F, 2.0F, 3.0F),
                PartPose.offset(0.0F, -1.4F, -1.9F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    public ModelPart head() {
        return head;
    }

    public void translateToBeak(com.mojang.blaze3d.vertex.PoseStack stack) {
        root.translateAndRotate(stack);
        body.translateAndRotate(stack);
        head.translateAndRotate(stack);
    }

    @Override
    public void setupAnim(CrowRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;
        float walkPos = state.walkAnimationPos;
        float walkSpeed = Math.min(state.walkAnimationSpeed, 1.0F);
        float fly = Mth.clamp(state.flyProgress, 0, 1);
        float sit = Mth.clamp(state.sitProgress, 0, 1);
        float bite = Mth.clamp(state.attackProgress, 0, 1);

        float run = Math.max(0, walkSpeed - fly);
        body.setRotation(1.0036F + rad(25) * bite + rad(20) * fly + rad(15) * run + rad(-25) * sit, 0, 0);
        head.setRotation(-0.7418F + rad(60) * bite + rad(-15) * fly + rad(-20) * run
                + rad(25) * sit + state.xRot * Mth.DEG_TO_RAD * 0.5F, state.yRot * Mth.DEG_TO_RAD * 0.7F, 0);
        legLeft.setRotation(0.5672F + rad(-25) * bite + rad(55) * fly + rad(-15) * run
                + rad(25) * sit + Mth.cos(walkPos * 1.2F) * 0.78F * 1.85F * walkSpeed * (1 - fly)
                - 0.2F * walkSpeed * (1 - fly), 0, 0);
        legRight.setRotation(0.5672F + rad(-25) * bite + rad(55) * fly + rad(-15) * run
                + rad(25) * sit + Mth.cos(walkPos * 1.2F + Mth.PI) * 0.78F * 1.85F * walkSpeed * (1 - fly)
                + 0.2F * walkSpeed * (1 - fly), 0, 0);
        tail.setRotation(-0.1309F + Mth.cos(age * 0.07F) * 0.02F, 0,
                Mth.cos(walkPos * 1.2F + 1F) * 0.156F * walkSpeed * (1 - fly));

        if (fly > 0.01F) {
            float flap = Mth.cos(age * 0.8F) * 0.6F * fly;
            wingRight.setRotation(0.0436F + rad(-90) * fly, rad(90) * fly + flap, 0);
            wingLeft.setRotation(0.0436F + rad(-90) * fly, rad(-90) * fly - flap, 0);
            wingRight.y = -4.9F + 2.0F * fly;
            wingRight.z = 1.7F + 1.0F * fly;
            wingLeft.y = -4.9F + 2.0F * fly;
            wingLeft.z = 1.7F + 1.0F * fly;
            body.y = 21.9F - Math.abs(Mth.sin(age * 0.4F)) * 0.8F * fly;
            head.xRot += (-Mth.cos(age * 0.8F + 2F) * 0.04F + 0.1F) * fly;
        } else {
            wingRight.setRotation(0.0436F, 0, 0);
            wingLeft.setRotation(0.0436F, 0, 0);
            wingRight.y = -4.9F;
            wingRight.z = 1.7F;
            wingLeft.y = -4.9F;
            wingLeft.z = 1.7F;
            body.y = 21.9F + Math.abs(Mth.sin(walkPos * 1.2F)) * 1.0F * walkSpeed;
            head.xRot += Mth.cos(walkPos * 1.2F + 2F) * 0.3F * walkSpeed;
        }
        if (state.isBaby) {
            head.xScale = 1.45F;
            head.yScale = 1.45F;
            head.zScale = 1.45F;
        } else {
            head.xScale = 0.9F;
            head.yScale = 0.9F;
            head.zScale = 0.9F;
        }
    }

    private static float rad(double deg) {
        return (float) Math.toRadians(deg);
    }
}
