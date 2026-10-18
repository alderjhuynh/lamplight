package com.aura.lamplight.mixin.ambrosia;

import com.aura.lamplight.ambrosia.effect.ModEffects;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class FamishedCanEatMixin {

    @Inject(method = "canEat", at = @At("HEAD"), cancellable = true)
    private void ambrosia$allowEatWhenFamished(boolean canAlwaysEat, CallbackInfoReturnable<Boolean> cir) {
        Player self = (Player) (Object) this;
        if (self.hasEffect(ModEffects.FAMISHED)) {
            cir.setReturnValue(true);
        }
    }
}
