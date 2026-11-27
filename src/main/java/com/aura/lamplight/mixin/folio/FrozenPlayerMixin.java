package com.aura.lamplight.mixin.folio;

import com.aura.lamplight.folio.effect.Frozen;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class FrozenPlayerMixin {

    @Inject(method = "cannotAttack", at = @At("HEAD"), cancellable = true)
    private void folio$frozenCannotAttack(final Entity entity, final CallbackInfoReturnable<Boolean> cir) {
        if (Frozen.is((Player) (Object) this)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "interactOn", at = @At("HEAD"), cancellable = true)
    private void folio$frozenCannotInteract(
        final Entity entity,
        final InteractionHand hand,
        final Vec3 location,
        final CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (Frozen.is((Player) (Object) this)) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }
}
