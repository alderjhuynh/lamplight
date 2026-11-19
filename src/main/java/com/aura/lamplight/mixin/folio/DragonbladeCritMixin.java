package com.aura.lamplight.mixin.folio;

import com.aura.lamplight.folio.server.DragonbladeHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class DragonbladeCritMixin {
    @Inject(method = "canCriticalAttack", at = @At("RETURN"), cancellable = true)
    private void lamplight$forceCrit(Entity target, CallbackInfoReturnable<Boolean> cir) {
        Player self = (Player) (Object) this;
        if (DragonbladeHandler.isForcedCrit(self)) {
            cir.setReturnValue(true);
            DragonbladeHandler.clearForcedCrit(self);
        }
    }
}
