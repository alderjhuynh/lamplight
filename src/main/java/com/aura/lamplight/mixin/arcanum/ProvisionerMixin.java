package com.aura.lamplight.mixin.arcanum;

import com.aura.lamplight.arcanum.enchantment.ArcanumEnchantments;
import com.aura.lamplight.arcanum.server.RebuildHandler;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class ProvisionerMixin {

    @Inject(method = "causeFoodExhaustion", at = @At("HEAD"), cancellable = true)
    private void lamplight$provisionerNoExhaustion(float exhaustion, CallbackInfo ci) {
        Player self = (Player) (Object) this;
        if (self.level().isClientSide()) return;
        if (ci.isCancelled()) return;
        if (RebuildHandler.armorLvl(self, ArcanumEnchantments.PROVISIONER) > 0) {
            ci.cancel();
        }
    }
}
