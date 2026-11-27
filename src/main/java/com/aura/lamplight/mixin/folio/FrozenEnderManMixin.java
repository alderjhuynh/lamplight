package com.aura.lamplight.mixin.folio;

import com.aura.lamplight.folio.effect.Frozen;
import net.minecraft.world.entity.monster.EnderMan;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderMan.class)
public abstract class FrozenEnderManMixin {

    @Inject(method = "teleport(DDD)Z", at = @At("HEAD"), cancellable = true)
    private void folio$frozenCannotTeleport(
        final double x,
        final double y,
        final double z,
        final CallbackInfoReturnable<Boolean> cir
    ) {
        if (Frozen.is((EnderMan) (Object) this)) {
            cir.setReturnValue(false);
        }
    }
}
