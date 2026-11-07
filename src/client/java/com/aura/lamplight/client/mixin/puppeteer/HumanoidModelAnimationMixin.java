package com.aura.lamplight.client.mixin.puppeteer;

import com.aura.lamplight.puppeteer.animation.AnimationChannel;
import com.aura.lamplight.puppeteer.animation.AnimationDefinition;
import com.aura.lamplight.puppeteer.animation.AnimationDefinitions;
import com.aura.lamplight.puppeteer.animation.BodyPart;
import com.aura.lamplight.puppeteer.animation.Vector3;
import com.aura.lamplight.puppeteer.client.AnimationClientState;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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

    @Unique
    private static final Map<ModelPart, Vector3> BASE_SCALES = new HashMap<>();

    @Inject(method = "setupAnim", at = @At("TAIL"))
    private void puppeteer$applyAnimation(final T state, final CallbackInfo ci) {
        AnimationClientState.Captured captured = ((FabricRenderState) state).getData(AnimationClientState.ANIMATION_RENDER_DATA);
        if (captured == null) {
            return;
        }
        AnimationClientState.ActiveSet activeSet = captured.set();
        float clock = captured.clock();
        if (activeSet == null) {
            puppeteer$restoreBaseScales();
            return;
        }
        if (activeSet.layers().isEmpty()) {
            puppeteer$restoreBaseScales();
        } else {
            for (ModelPart part : new ModelPart[] {body, head, rightArm, leftArm, rightLeg, leftLeg}) {
                BASE_SCALES.computeIfAbsent(part, key -> new Vector3(key.xScale, key.yScale, key.zScale));
            }
        }

        Map<Identifier, AnimationClientState.Seen> seen = AnimationClientState.trackSeen(activeSet, clock);
        List<AnimationClientState.Layer> layers = activeSet.layers();

        boolean willAnimate = !layers.isEmpty() || !seen.isEmpty();
        if (!willAnimate) {
            AnimationClientState.pruneSeen(activeSet, clock);
            return;
        }
        body.resetPose();
        rightArm.resetPose();
        leftArm.resetPose();
        rightLeg.resetPose();
        leftLeg.resetPose();
        ((Model<?>) (Object) this).root().resetPose();

        for (AnimationClientState.Layer layer : layers) {
            AnimationClientState.Seen entry = seen.get(layer.animationId());
            float weight = Math.min(1.0F, (clock - entry.firstSeen()) / AnimationClientState.FADE_TICKS);
            if (weight > 0.0F) {
                puppeteer$applyLayer(layer, weight, AnimationClientState.phaseOf(activeSet, layer.animationId()));
            }
        }

        for (Map.Entry<Identifier, AnimationClientState.Seen> ghost : seen.entrySet()) {
            boolean present = false;
            for (AnimationClientState.Layer layer : layers) {
                present |= layer.animationId().equals(ghost.getKey());
            }
            if (present) {
                continue;
            }
            float since = clock - ghost.getValue().lastPresent();
            if (since < 0.0F || since > AnimationClientState.FADE_TICKS) {
                continue;
            }
            float weight = 1.0F - since / AnimationClientState.FADE_TICKS;
            if (weight > 0.0F) {
                puppeteer$applyLayer(new AnimationClientState.Layer(ghost.getKey(), 0L), weight,
                    AnimationClientState.phaseOf(activeSet, ghost.getKey()));
            }
        }

        AnimationClientState.pruneSeen(activeSet, clock);
    }

    @Unique
    private void puppeteer$restoreBaseScales() {
        for (Map.Entry<ModelPart, Vector3> entry : BASE_SCALES.entrySet()) {
            entry.getKey().xScale = entry.getValue().x();
            entry.getKey().yScale = entry.getValue().y();
            entry.getKey().zScale = entry.getValue().z();
        }
    }

    @Unique
    private void puppeteer$applyLayer(
        final AnimationClientState.Layer layer,
        final float weight,
        final float phase
    ) {
        AnimationDefinition definition = AnimationDefinitions.byId(layer.animationId());
        if (definition == null || weight <= 0.0F) {
            return;
        }

        float tick = phase;
        float length = definition.lengthTicks();
        if (definition.loop() && !definition.holdLastFrame()) {
            if (length > 0.0F) {
                tick = phase % length;
            }
        } else {
            tick = Math.min(phase, length);
        }

        for (AnimationChannel channel : definition.channels()) {
            Vector3[] sampled = channel.sample(tick,
                definition.loop() && !definition.holdLastFrame() ? definition.lengthTicks() : 0.0F);
            for (ModelPart part : puppeteer$partsFor(channel.part())) {
                if (part == null) {
                    continue;
                }
                Vector3 rotation = sampled[0];
                Vector3 translation = sampled[1];

                if (!channel.scaleKeyframes().isEmpty()) {
                    part.xScale *= lerp(1.0F, sampled[2].x(), weight);
                    part.yScale *= lerp(1.0F, sampled[2].y(), weight);
                    part.zScale *= lerp(1.0F, sampled[2].z(), weight);
                }

                part.xRot += (float) Math.toRadians(rotation.x() * weight);
                part.yRot += (float) Math.toRadians(rotation.y() * weight);
                part.zRot += (float) Math.toRadians(rotation.z() * weight);
                part.x += translation.x() * weight;
                part.y += translation.y() * weight;
                part.z += translation.z() * weight;
            }
        }
    }

    @Unique
    private static float lerp(final float a, final float b, final float t) {
        return a + (b - a) * t;
    }

    @Unique
    private ModelPart @Nullable [] puppeteer$partsFor(final BodyPart part) {
        return switch (part) {
            case HEAD -> new ModelPart[] {head};
            case BODY -> new ModelPart[] {body};
            case RIGHT_ARM -> new ModelPart[] {rightArm};
            case LEFT_ARM -> new ModelPart[] {leftArm};
            case RIGHT_LEG -> new ModelPart[] {rightLeg};
            case LEFT_LEG -> new ModelPart[] {leftLeg};
            case WAIST -> new ModelPart[] {body, head, rightArm, leftArm};
            case ROOT -> new ModelPart[] {((Model<?>) (Object) this).root()};
        };
    }
}
