package com.aura.lamplight.client.mixin.panacea;

import com.aura.lamplight.panacea.client.render.ShroudedRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    private void panacea$applyTrueSightAndShroud(LivingEntity entity, LivingEntityRenderState state, float partialTicks, CallbackInfo ci) {
        MobEffectInstance invisibility = entity.getEffect(MobEffects.INVISIBILITY);
        int amplifier = invisibility == null ? -1 : invisibility.getAmplifier();
        ((ShroudedRenderState) state).panacea$setInvisibilityAmplifier(amplifier);

        LocalPlayer viewer = Minecraft.getInstance().player;
        if (viewer != null && !viewer.isSpectator()) {
            MobEffectInstance nightVision = viewer.getEffect(MobEffects.NIGHT_VISION);
            boolean trueSight = nightVision != null && nightVision.getAmplifier() >= 1;

            if (trueSight && amplifier >= 0 && state.isInvisibleToPlayer) {
                state.isInvisibleToPlayer = false;
            }
        }

        if (amplifier >= 1) {
            if (state instanceof HumanoidRenderState humanoid) {
                humanoid.headEquipment = ItemStack.EMPTY;
                humanoid.chestEquipment = ItemStack.EMPTY;
                humanoid.legsEquipment = ItemStack.EMPTY;
                humanoid.feetEquipment = ItemStack.EMPTY;
                humanoid.rightHandItemStack = ItemStack.EMPTY;
                humanoid.leftHandItemStack = ItemStack.EMPTY;
                humanoid.rightHandItemState.clear();
                humanoid.leftHandItemState.clear();
            }
            state.headItem.clear();
        }
    }

    @Inject(method = "getModelTint(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;)I", at = @At("RETURN"), cancellable = true)
    private void panacea$trueSightAlpha(LivingEntityRenderState state, CallbackInfoReturnable<Integer> cir) {
        int amplifier = ((ShroudedRenderState) state).panacea$getInvisibilityAmplifier();
        if (amplifier < 0 || !state.isInvisible) {
            return;
        }

        LocalPlayer viewer = Minecraft.getInstance().player;
        if (viewer == null || viewer.isSpectator()) {
            return;
        }

        MobEffectInstance nightVision = viewer.getEffect(MobEffects.NIGHT_VISION);
        if (nightVision == null || nightVision.getAmplifier() < 1) {
            return;
        }

        if (amplifier >= 1) {
            cir.setReturnValue(ARGB.color(60, 255, 255, 255));
        } else {
            cir.setReturnValue(ARGB.color(255, 255, 255, 255));
        }
    }
}
