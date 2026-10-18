package com.aura.lamplight.client.mixin.puppeteer;

import com.aura.lamplight.puppeteer.animation.AnimationChannel;
import com.aura.lamplight.puppeteer.animation.AnimationDefinition;
import com.aura.lamplight.puppeteer.animation.AnimationDefinitions;
import com.aura.lamplight.puppeteer.animation.BodyPart;
import com.aura.lamplight.puppeteer.animation.Vector3;
import com.aura.lamplight.puppeteer.client.AnimationClientState;
import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelAnimationMixin<T extends HumanoidRenderState> {

    @Shadow
    public ModelPart head;
    @Shadow
    public ModelPart body;
    @Shadow
    public ModelPart rightArm;
    @Shadow
    public ModelPart leftArm;
    @Shadow
    public ModelPart rightLeg;
    @Shadow
    public ModelPart leftLeg;

    @Inject(method = "setupAnim", at = @At("TAIL"))
    private void puppeteer$applyAnimation(final T state, final CallbackInfo ci) {
        AnimationClientState.Active active = ((FabricRenderState) state).getData(AnimationClientState.ANIMATION_RENDER_DATA);
        if (active == null) {
            return;
        }

        AnimationDefinition definition = AnimationDefinitions.byId(active.animationId());
        if (definition == null) {
            return;
        }

        float tick = computeTick(definition, active);

        for (AnimationChannel channel : definition.channels()) {
            ModelPart part = partFor(channel.part());
            if (part == null) {
                continue;
            }
            Vector3[] sample = channel.sample(tick);
            Vector3 rotation = sample[0];
            Vector3 translation = sample[1];

            part.xRot += (float) Math.toRadians(rotation.x());
            part.yRot += (float) Math.toRadians(rotation.y());
            part.zRot += (float) Math.toRadians(rotation.z());
            part.x += translation.x();
            part.y += translation.y();
            part.z += translation.z();
        }
    }

    private static float computeTick(final AnimationDefinition definition, final AnimationClientState.Active active) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            return 0.0F;
        }

        long elapsed = client.level.getGameTime() - active.startGameTime();
        if (elapsed <= 0) {
            return 0.0F;
        }

        if (definition.loop() && definition.lengthTicks() > 0) {
            elapsed = elapsed % definition.lengthTicks();
        }
        return (float) elapsed;
    }

    private @Nullable ModelPart partFor(final BodyPart part) {
        return switch (part) {
            case HEAD -> head;
            case BODY -> body;
            case RIGHT_ARM -> rightArm;
            case LEFT_ARM -> leftArm;
            case RIGHT_LEG -> rightLeg;
            case LEFT_LEG -> leftLeg;
        };
    }
}
