package com.aura.lamplight.mixin.folio;

import com.aura.lamplight.folio.effect.Frozen;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class FrozenLivingEntityMixin {

    @WrapOperation(
        method = "aiStep",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isImmobile()Z")
    )
    private boolean folio$frozenIsImmobile(final LivingEntity self, final Operation<Boolean> original) {
        return Frozen.is(self) || original.call(self);
    }

    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    private void folio$frozenCannotStrike(
        final ServerLevel level,
        final DamageSource source,
        final float damage,
        final CallbackInfoReturnable<Boolean> cir
    ) {
        if (source.getDirectEntity() != null
            && source.getDirectEntity() == source.getEntity()
            && source.getDirectEntity() != (Object) this
            && Frozen.is(source.getDirectEntity())) {
            cir.setReturnValue(false);
        }
    }
}
