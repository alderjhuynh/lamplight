package com.aura.lamplight.client.mixin.folio;

import com.aura.lamplight.folio.client.DragonbladeGrayscaleAccessor;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public abstract class DragonbladeRenderStateMixin implements DragonbladeGrayscaleAccessor {
    @Unique
    private boolean lamplight$flag = false;

    @Override
    public boolean lamplight$isDragonbladeGrayscale() {
        return lamplight$flag;
    }

    @Override
    public void lamplight$setDragonbladeGrayscale(boolean value) {
        lamplight$flag = value;
    }
}
