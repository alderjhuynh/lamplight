package com.aura.lamplight.client.mixin.picturesque;

import com.aura.lamplight.picturesque.client.splash.SplashManager;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class SplashEntityMixin {
    @Unique
    private boolean picturesque$wasInLava;

    @Inject(method = "doWaterSplashEffect", at = @At("HEAD"))
    private void picturesque$waterSplash(CallbackInfo ci) {
        SplashManager.get().onFluidEntry((Entity) (Object) this, false);
    }

    @Inject(method = "updateFluidInteraction", at = @At("RETURN"))
    private void picturesque$lavaSplash(CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;
        boolean inLava = self.isInLava();
        if (inLava && !this.picturesque$wasInLava) {
            SplashManager.get().onFluidEntry(self, true);
        }
        this.picturesque$wasInLava = inLava;
    }
}
