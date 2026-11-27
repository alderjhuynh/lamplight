package com.aura.lamplight.mixin.folio;

import com.aura.lamplight.folio.effect.Frozen;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Creeper.class)
public abstract class FrozenCreeperMixin {

    @WrapOperation(
        method = "tick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Creeper;getSwellDir()I"))
    private int folio$frozenDefuse(final Creeper creeper, final Operation<Integer> original) {
        return Frozen.is(creeper) ? -1 : original.call(creeper);
    }
}
