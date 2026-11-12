package com.aura.lamplight.mixin.arcanum;

import com.aura.lamplight.arcanum.server.RebuildHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
@SuppressWarnings ("all")
public class WhirlwindMixin {

    @Inject(method = "doSweepAttack", at = @At("TAIL"))
    private void lamplight$whirlwindSweep(Entity target, float amount, DamageSource source, float knockback,
                                          CallbackInfo ci) {
        Player self = (Player) (Object) this;
        if (self.level() instanceof ServerLevel level) {
            RebuildHandler.onSweep(self, target, source);
        }
    }

    @Inject(method = "baseDamageScaleFactor", at = @At("RETURN"))
    private void lamplight$swingScale(CallbackInfoReturnable<Float> cir) {
        RebuildHandler.onSwingScale((Player) (Object) this, cir.getReturnValue());
    }
}
