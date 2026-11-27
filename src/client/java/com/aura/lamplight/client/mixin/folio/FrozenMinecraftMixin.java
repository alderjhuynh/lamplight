package com.aura.lamplight.client.mixin.folio;

import com.aura.lamplight.folio.effect.Frozen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class FrozenMinecraftMixin {

    @Shadow
    public @Nullable LocalPlayer player;

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void folio$frozenNoAttack(final CallbackInfoReturnable<Boolean> cir) {
        if (this.player != null && Frozen.is(this.player)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void folio$frozenNoUse(final CallbackInfo ci) {
        if (this.player != null && Frozen.is(this.player)) {
            ci.cancel();
        }
    }

    @ModifyVariable(method = "continueAttack", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private boolean folio$frozenNoMining(final boolean down) {
        return down && !(this.player != null && Frozen.is(this.player));
    }
}
