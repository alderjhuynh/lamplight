package com.aura.lamplight.mixin.arcanum;

import com.aura.lamplight.arcanum.enchantment.ArcanumEnchantments;
import com.aura.lamplight.arcanum.server.RebuildHandler;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class MiserMixin {

    @Inject(method = "causeFoodExhaustion", at = @At("HEAD"), cancellable = true)
    private void lamplight$miser(float exhaustion, CallbackInfo ci) {
        if (ci.isCancelled()) return; // e.g. Provisioner fully stops exhaustion
        Player self = (Player) (Object) this;
        if (self.level().isClientSide() || !self.isSprinting()) return;
        int lvl = RebuildHandler.armorLvl(self, ArcanumEnchantments.MISER);
        if (lvl <= 0) return;
        ci.cancel();
        if (!self.getAbilities().invulnerable) {
            self.getFoodData().addExhaustion(exhaustion * (1.0F - 0.2F * lvl));
        }
    }
}
