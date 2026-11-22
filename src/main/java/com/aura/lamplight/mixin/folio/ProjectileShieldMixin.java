package com.aura.lamplight.mixin.folio;

import com.aura.lamplight.folio.server.ShieldHandler;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Projectile.class)
public abstract class ProjectileShieldMixin {

    @Inject(method = "hitTargetOrDeflectSelf", at = @At("HEAD"), cancellable = true)
    private void lamplight$shieldBlock(final HitResult hitResult, final CallbackInfoReturnable<ProjectileDeflection> cir) {
        if (ShieldHandler.tryBlock((Projectile) (Object) this)) {
            cir.setReturnValue(ProjectileDeflection.NONE);
        }
    }
}
