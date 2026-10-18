package com.aura.lamplight.mixin.ambrosia;

import com.aura.lamplight.ambrosia.effect.ModEffects;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class NourishmentExhaustionMixin {

    @Inject(method = "causeFoodExhaustion", at = @At("HEAD"), cancellable = true)
    private void ambrosia$blockExhaustion(float exhaustion, CallbackInfo ci) {
        Player self = (Player) (Object) this;
        if (self.hasEffect(ModEffects.NOURISHMENT)) {
            ci.cancel();
        }
    }
}
