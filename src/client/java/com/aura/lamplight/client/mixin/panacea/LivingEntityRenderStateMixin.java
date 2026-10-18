package com.aura.lamplight.client.mixin.panacea;

import com.aura.lamplight.panacea.client.render.ShroudedRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin implements ShroudedRenderState {
    @Unique
    private int panacea$invisibilityAmplifier = -1;

    @Override
    public int panacea$getInvisibilityAmplifier() {
        return this.panacea$invisibilityAmplifier;
    }

    @Override
    public void panacea$setInvisibilityAmplifier(int amplifier) {
        this.panacea$invisibilityAmplifier = amplifier;
    }
}
